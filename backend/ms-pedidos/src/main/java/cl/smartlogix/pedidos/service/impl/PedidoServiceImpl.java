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

import java.time.LocalDate;
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
    @Transactional(readOnly = true)
    public Pedido obtenerPedidoPorId(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Pedido obtenerPedidoPorNumeroOrden(String numeroOrden) {
        return pedidoRepository.findByNumeroOrden(numeroOrden)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con Orden Nº: " + numeroOrden));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Pedido> listarPedidos() {
        return pedidoRepository.findAll();
    }

    @Override
    @Transactional(noRollbackFor = { ResourceNotFoundException.class, DomainException.class })
    public Pedido crearPedido(CrearPedidoRequestDTO request) {
        
        // 1. Crear número de orden personalizado (ORD-AAAAMMDD-XXXX)
        String numeroOrden = "ORD-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + 
                             "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();

        Pedido pedido = Pedido.builder()
                .numeroOrden(numeroOrden)
                .fechaPedido(LocalDateTime.now())
                .estado(EstadoPedido.PENDIENTE)
                .totalCompra(0)
                .build();

        int totalCompraAcumulado = 0;

        // 2. Poblar los detalles calculando en Integer
        for (CrearPedidoRequestDTO.DetalleRequestDTO item : request.getItems()) {
            int subtotalItem = item.getPrecioUnitario() * item.getCantidad();
            totalCompraAcumulado += subtotalItem;

            DetallePedido detalle = DetallePedido.builder()
                    .productoId(item.getProductoId())
                    .sku(item.getSku())
                    .nombreProducto(item.getNombreProducto())
                    .precioUnitario(item.getPrecioUnitario())
                    .cantidad(item.getCantidad())
                    .subtotal(subtotalItem)
                    .build();

            pedido.agregarDetalle(detalle);
        }

        pedido.setTotalCompra(totalCompraAcumulado);
        pedido = pedidoRepository.save(pedido);
        log.info("Pedido {} registrado temporalmente en PENDIENTE. Total: ${}", pedido.getNumeroOrden(), totalCompraAcumulado);

        List<DetallePedido> productosConStockReservado = new ArrayList<>();

        try {
            // 3. Orquestación SAGA: Llamar síncronamente al ms-inventario producto por producto
            for (DetallePedido detalle : pedido.getDetalles()) {
                ReservarStockRequestDTO stockRequest = new ReservarStockRequestDTO(detalle.getProductoId(), detalle.getCantidad());
                
                // Petición HTTP síncrona vía Feign
                inventarioClient.reservarStock(stockRequest); 
                
                productosConStockReservado.add(detalle);
                log.info("Stock reservado OK para producto ID: {} (Cant: {})", detalle.getProductoId(), detalle.getCantidad());
            }

            // 4. Éxito SAGA: Cambiar estado a APROBADO
            pedido.setEstado(EstadoPedido.APROBADO);
            pedido = pedidoRepository.save(pedido);

            // Emitir evento a ms-envios
            List<PedidoAprobadoEventDTO.ItemEventDTO> eventItems = pedido.getDetalles().stream()
                .map(d -> new PedidoAprobadoEventDTO.ItemEventDTO(d.getProductoId(), d.getCantidad()))
                .collect(Collectors.toList());

            PedidoAprobadoEventDTO aprobadoEvent = PedidoAprobadoEventDTO.builder()
                    .pedidoId(pedido.getId())
                    .numeroOrden(pedido.getNumeroOrden())
                    .items(eventItems)
                    .build();

            eventPublisher.publicarPedidoAprobado(aprobadoEvent);
            log.info("SAGA Finalizada con Éxito. Pedido {} APROBADO.", pedido.getNumeroOrden());

        } catch (Exception e) {
            // 5. Fallo SAGA: Cambiar estado a RECHAZADO y enviar eventos de compensación (Rollback)
            pedido.setEstado(EstadoPedido.RECHAZADO);
            pedidoRepository.save(pedido);
            log.error("Fallo detectado en el flujo del pedido {}. Motivo: {}", pedido.getNumeroOrden(), e.getMessage());

            ejecutarCompensacionesStock(pedido, productosConStockReservado);

            if (e instanceof DomainException || e instanceof ResourceNotFoundException) {
                throw (RuntimeException) e;
            }
            throw new DomainException("No se pudo procesar el pedido debido a un error en el sistema central: " + e.getMessage());
        }

        return pedido;
    }

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
                log.error("💥 ERROR CRÍTICO: No se pudo enviar evento de compensación a RabbitMQ para Producto ID: {}. Causa: {}", 
                        detalle.getProductoId(), ex.getMessage());
            }
        }
    }
}