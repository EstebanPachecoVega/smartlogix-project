package cl.smartlogix.pedidos.service.impl;

import cl.smartlogix.pedidos.client.InventarioClient;
import cl.smartlogix.pedidos.dto.event.PedidoAprobadoEventDTO;
import cl.smartlogix.pedidos.dto.event.PedidoRechazadoEventDTO;
import cl.smartlogix.pedidos.dto.request.CrearPedidoRequestDTO;
import cl.smartlogix.pedidos.dto.request.ReservarStockRequestDTO;
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
import java.util.ArrayList;
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
        log.info("Iniciando creación de pedido para el usuario: {}", request.getUsuarioId());

        // 1. Instanciar el objeto Pedido manteniendo el correlativo único y agregando
        // la metadata logística
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
        List<DetallePedido> productosConStockReservado = new ArrayList<>();

        try {
            // 2. Mapear y calcular subtotales de la lista de ítems entrantes
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

            // 3. Guardar el estado inicial (PENDIENTE) en la base de datos local
            pedido = pedidoRepository.save(pedido);
            log.info("Pedido guardado localmente en estado PENDIENTE con orden: {}", pedido.getNumeroOrden());

            // 4. Ejecutar el pipeline de reservas distribuidas sincrónicas mediante Feign
            for (DetallePedido detalle : pedido.getDetalles()) {
                log.debug("Solicitando reserva de stock para Producto ID: {}, Cantidad: {}", detalle.getProductoId(),
                        detalle.getCantidad());

                ReservarStockRequestDTO reservaDto = new ReservarStockRequestDTO(detalle.getProductoId(),
                        detalle.getCantidad());

                // Llamada Feign hacia ms-inventario
                inventarioClient.reservarStock(reservaDto);

                // Si la llamada fue exitosa, se añade a la lista de "guardias" por si ocurre un
                // fallo posterior
                productosConStockReservado.add(detalle);
            }

            // 5. DISPARADOR DE ÉXITO INTEGRADO: Si todas las reservas fueron exitosas, se
            // aprueba el pedido y se publica el evento enriquecido
            pedido.setEstado(EstadoPedido.APROBADO);
            pedidoRepository.save(pedido);
            log.info("Pedido {} APROBADO exitosamente. Transacción local consolidada.", pedido.getNumeroOrden());

            // Construir el evento enriquecido con los datos exactos que espera el ms-envios
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

            // Despachar evento asíncrono a RabbitMQ
            eventPublisher.publicarPedidoAprobado(eventoAprobado);

        } catch (Exception e) {
            // 6. BLOQUE DE RESILIENCIA E INTACTO (SAGA / Compensaciones)
            pedido.setEstado(EstadoPedido.RECHAZADO);
            pedidoRepository.save(pedido);
            log.error("Fallo detectado en el flujo del pedido {}. Motivo: {}", pedido.getNumeroOrden(), e.getMessage());

            // Ejecutar compensaciones de stock para cada producto que se reservó
            // exitosamente antes del fallo
            ejecutarCompensacionesStock(pedido, productosConStockReservado);

            if (e instanceof DomainException || e instanceof ResourceNotFoundException) {
                throw (RuntimeException) e;
            }
            throw new DomainException(
                    "No se pudo procesar el pedido debido a un error en el sistema central: " + e.getMessage());
        }

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

    /*
     * Método privado para ejecutar compensaciones de stock en caso de fallo en el
     * proceso de creación del pedido
     */
    private void ejecutarCompensacionesStock(Pedido pedido, List<DetallePedido> productosAReversar) {
        for (DetallePedido detalle : productosAReversar) {
            try {
                PedidoRechazadoEventDTO compensacion = PedidoRechazadoEventDTO.builder()
                        .pedidoId(pedido.getId())
                        .numeroOrden(pedido.getNumeroOrden())
                        .productoId(detalle.getProductoId())
                        .cantidad(detalle.getCantidad())
                        .build();

                eventPublisher.publicarPedidoRechazado(compensacion);
            } catch (Exception ex) {
                log.error(
                        "ERROR CRÍTICO: No se pudo enviar evento de compensación a RabbitMQ para Producto ID: {}. Causa: {}",
                        detalle.getProductoId(), ex.getMessage());
            }
        }
    }

    @Override
    @Transactional
    public void actualizarEstadoPorEnvio(Long pedidoId, String estadoEnvio) {
        log.info("Sincronizando Pedido ID: {} con novedad de logística: [{}]", pedidoId, estadoEnvio);

        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con ID: " + pedidoId));

        EstadoPedido estadoAnterior = pedido.getEstado();

        switch (estadoEnvio) {

            // 1. Estados iniciales en bodega (El pedido se mantiene en APROBADO)
            case "PENDIENTE":
            case "PREPARANDO":
                pedido.setEstado(EstadoPedido.APROBADO);
                break;

            // 2. Estados de movimiento y logística viva (El pedido pasa a EN_CAMINO)
            case "ENVIADO":
            case "EN_TRANSITO":
            case "EN_REPARTO":
            case "RETRASADO":
            case "INTENTO_FALLIDO":
                pedido.setEstado(EstadoPedido.EN_CAMINO);
                break;

            // 3. Estado de éxito final (El pedido pasa a ENTREGADO)
            case "ENTREGADO":
                pedido.setEstado(EstadoPedido.ENTREGADO);
                break;

            // 4. Estados terminales de fallo (El pedido pasa a RECHAZADO para activar
            // devoluciones o notas de crédito)
            case "DEVUELTO":
            case "CANCELADO":
                pedido.setEstado(EstadoPedido.RECHAZADO);
                break;

            default:
                log.warn("Se recibió un estado de envío desconocido ('{}'). No se alteró el pedido.", estadoEnvio);
                return;
        }

        // Solo guardamos si el estado realmente cambió para evitar escrituras
        // innecesarias en BD
        if (estadoAnterior != pedido.getEstado()) {
            pedidoRepository.save(pedido);
            log.info("SAGA TRANSICIÓN: Pedido N° {} cambió de {} a {}",
                    pedido.getNumeroOrden(), estadoAnterior, pedido.getEstado());
        } else {
            log.info("El estado del pedido se mantiene en {} (No requiere actualización visual)",
                    pedido.getEstado());
        }
    }
}