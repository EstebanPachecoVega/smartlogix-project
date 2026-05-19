package cl.smartlogix.pedidos.service.impl;

import cl.smartlogix.pedidos.client.InventarioClient;
import cl.smartlogix.pedidos.dto.event.PedidoAprobadoEventDTO;
import cl.smartlogix.pedidos.dto.event.PedidoRechazadoEventDTO;
import cl.smartlogix.pedidos.dto.request.*;
import cl.smartlogix.pedidos.dto.response.PedidoStockResponseDTO;
import cl.smartlogix.pedidos.entity.DetallePedido;
import cl.smartlogix.pedidos.entity.EstadoPedido;
import cl.smartlogix.pedidos.entity.Pedido;
import cl.smartlogix.pedidos.exception.DomainException;
import cl.smartlogix.pedidos.exception.ResourceNotFoundException;
import cl.smartlogix.pedidos.publisher.PedidoEventPublisher;
import cl.smartlogix.pedidos.repository.PedidoRepository;
import cl.smartlogix.pedidos.service.PedidoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PedidoServiceImpl implements PedidoService {

    private final PedidoRepository pedidoRepository;
    private final InventarioClient inventarioClient;
    private final PedidoEventPublisher eventPublisher;

    @Override
    @Transactional
    public Pedido crearPedido(CrearPedidoRequestDTO request) {
        log.info("Creando pedido para usuario: {}", request.getUsuarioId());

        // 1. Construir pedido
        Pedido pedido = Pedido.builder()
                .numeroOrden("ORD-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-"
                        + UUID.randomUUID().toString().substring(0, 4).toUpperCase())
                .fechaPedido(LocalDateTime.now())
                .estado(EstadoPedido.PENDIENTE)
                .usuarioId(request.getUsuarioId())
                .destinatario(request.getDestinatario())
                .calle(request.getCalle())
                .numero(request.getNumero())
                .comuna(request.getComuna())
                .ciudad(request.getCiudad())
                .codigoPostal(request.getCodigoPostal())
                .metodoEnvio(request.getMetodoEnvio())
                .pesoKg(request.getPesoKg())
                .dimensiones(request.getDimensiones())
                .totalCompra(0)
                .build();

        int totalAcumulado = 0;
        for (CrearPedidoRequestDTO.DetalleRequestDTO itemDto : request.getItems()) {
            int subtotal = itemDto.getPrecioUnitario() * itemDto.getCantidad();
            totalAcumulado += subtotal;
            DetallePedido detalle = DetallePedido.builder()
                    .productoId(itemDto.getProductoId())
                    .sku(itemDto.getSku())
                    .nombreProducto(itemDto.getNombreProducto())
                    .precioUnitario(itemDto.getPrecioUnitario())
                    .cantidad(itemDto.getCantidad())
                    .subtotal(subtotal)
                    .build();
            pedido.agregarDetalle(detalle);
        }
        pedido.setTotalCompra(totalAcumulado);

        // 2. Guardar pedido (estado PENDIENTE)
        pedido = pedidoRepository.save(pedido);
        log.info("Pedido guardado en estado PENDIENTE con ID: {}, Número: {}", pedido.getId(), pedido.getNumeroOrden());

        String reservaId = pedido.getId().toString();
        List<ReservarStockRequestDTO> itemsReserva = pedido.getDetalles().stream()
                .map(d -> new ReservarStockRequestDTO(d.getProductoId(), d.getCantidad(), reservaId))
                .collect(Collectors.toList());

        // 3. Reservar stock (una sola llamada atómica)
        try {
            PedidoStockRequestDTO reservaRequest = new PedidoStockRequestDTO(itemsReserva, reservaId);
            PedidoStockResponseDTO reservaResponse = inventarioClient.reservarStock(reservaRequest);
            if (!reservaResponse.getReservaId().equals(reservaId)) {
                throw new DomainException("El ID de reserva devuelto no coincide");
            }
            log.info("Reserva exitosa para pedido {}", reservaId);
        } catch (Exception e) {
            // Falló la reserva → no hay nada que compensar (reserva atómica falló por
            // completo)
            pedido.setEstado(EstadoPedido.RECHAZADO);
            pedidoRepository.save(pedido);
            log.error("Fallo en reserva de stock para pedido {}: {}", pedido.getNumeroOrden(), e.getMessage());
            throw new DomainException("No se pudo reservar stock: " + e.getMessage());
        }

        // 4. Confirmar reserva (descuento definitivo)
        try {
            List<ConfirmarReservaRequestDTO.ItemConfirmacionDTO> itemsConfirm = pedido.getDetalles().stream()
                    .map(d -> new ConfirmarReservaRequestDTO.ItemConfirmacionDTO(d.getProductoId(), d.getCantidad()))
                    .collect(Collectors.toList());
            ConfirmarReservaRequestDTO confirmRequest = new ConfirmarReservaRequestDTO(reservaId, itemsConfirm);
            inventarioClient.confirmarReserva(confirmRequest);
            log.info("Reserva confirmada para pedido {}", reservaId);
        } catch (Exception e) {
            // Falló la confirmación → hay que liberar el stock que ya se reservó
            pedido.setEstado(EstadoPedido.RECHAZADO);
            pedidoRepository.save(pedido);
            log.error("Fallo en confirmación de reserva para pedido {}: {}", pedido.getNumeroOrden(), e.getMessage());
            for (DetallePedido detalle : pedido.getDetalles()) {
                PedidoRechazadoEventDTO compensacion = PedidoRechazadoEventDTO.builder()
                        .pedidoId(pedido.getId())
                        .numeroOrden(pedido.getNumeroOrden())
                        .productoId(detalle.getProductoId())
                        .cantidad(detalle.getCantidad())
                        .reservaId(reservaId)
                        .build();
                eventPublisher.publicarPedidoRechazado(compensacion);
            }
            throw new DomainException("No se pudo confirmar la reserva: " + e.getMessage());
        }

        // 5. Aprobar pedido y publicar evento
        pedido.setEstado(EstadoPedido.APROBADO);
        pedidoRepository.save(pedido);
        log.info("Pedido {} APROBADO", pedido.getNumeroOrden());

        PedidoAprobadoEventDTO eventoAprobado = PedidoAprobadoEventDTO.builder()
                .pedidoId(pedido.getId())
                .numeroOrden(pedido.getNumeroOrden())
                .usuarioId(pedido.getUsuarioId())
                .destinatario(pedido.getDestinatario())
                .calle(pedido.getCalle())
                .numero(pedido.getNumero())
                .comuna(pedido.getComuna())
                .ciudad(pedido.getCiudad())
                .codigoPostal(pedido.getCodigoPostal())
                .metodoEnvio(pedido.getMetodoEnvio())
                .pesoKg(pedido.getPesoKg())
                .dimensiones(pedido.getDimensiones())
                .build();
        eventPublisher.publicarPedidoAprobado(eventoAprobado);

        return pedido;
    }

    @Override
    @Transactional(readOnly = true)
    public Pedido obtenerPedidoPorId(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Pedido obtenerPedidoPorNumeroOrden(String numeroOrden) {
        return pedidoRepository.findByNumeroOrden(numeroOrden)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Pedido no encontrado con número de orden: " + numeroOrden));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Pedido> listarPedidos() {
        return pedidoRepository.findAll();
    }

    @Override
    @Transactional
    public void actualizarEstadoPorEnvio(Long pedidoId, String estadoEnvio) {
        log.info("Sincronizando Pedido ID: {} con novedad de logística: [{}]", pedidoId, estadoEnvio);
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con ID: " + pedidoId));
        EstadoPedido estadoAnterior = pedido.getEstado();

        switch (estadoEnvio) {
            case "PENDIENTE":
            case "PREPARANDO":
                pedido.setEstado(EstadoPedido.APROBADO);
                break;
            case "ENVIADO":
            case "EN_TRANSITO":
            case "EN_REPARTO":
            case "RETRASADO":
            case "INTENTO_FALLIDO":
                pedido.setEstado(EstadoPedido.EN_CAMINO);
                break;
            case "ENTREGADO":
                pedido.setEstado(EstadoPedido.ENTREGADO);
                break;
            case "DEVUELTO":
            case "CANCELADO":
                pedido.setEstado(EstadoPedido.RECHAZADO);
                break;
            default:
                log.warn("Estado de envío desconocido: '{}'", estadoEnvio);
                return;
        }

        if (estadoAnterior != pedido.getEstado()) {
            pedidoRepository.save(pedido);
            log.info("Pedido {} cambió de {} a {}", pedido.getNumeroOrden(), estadoAnterior, pedido.getEstado());
        }
    }
}