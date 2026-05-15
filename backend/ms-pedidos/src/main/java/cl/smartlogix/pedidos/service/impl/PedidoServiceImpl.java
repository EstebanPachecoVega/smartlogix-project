package cl.smartlogix.pedidos.service.impl;

import cl.smartlogix.pedidos.client.InventarioClient;
import cl.smartlogix.pedidos.dto.event.PedidoAprobadoEventDTO;
import cl.smartlogix.pedidos.dto.request.CrearPedidoRequestDTO;
import cl.smartlogix.pedidos.dto.request.ReservarStockRequestDTO;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class PedidoServiceImpl implements PedidoService {
    private final PedidoRepository pedidoRepository;
    private final InventarioClient inventarioClient;
    private final PedidoEventPublisher eventPublisher;

    @Override
    public Pedido obtenerPedidoPorId(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con ID: " + id));
    }

    @Override
    @Transactional(noRollbackFor = {ResourceNotFoundException.class, DomainException.class})
    public Pedido crearPedido(CrearPedidoRequestDTO request) {
        // 1. Guardar pedido en estado PENDIENTE
        Pedido pedido = new Pedido();
        pedido.setProductoId(request.getProductoId());
        pedido.setCantidad(request.getCantidad());
        pedido.setEstado(EstadoPedido.PENDIENTE);
        pedido = pedidoRepository.save(pedido);
        log.info("Pedido {} creado en estado PENDIENTE", pedido.getId());

        // 2. Llamar a Inventario para reservar stock
        try {
            ReservarStockRequestDTO stockRequest = new ReservarStockRequestDTO();
            stockRequest.setProductoId(request.getProductoId());
            stockRequest.setCantidad(request.getCantidad());
            inventarioClient.reservarStock(stockRequest);
            log.info("Stock reservado exitosamente para pedido {}", pedido.getId());

            // 3. Éxito: actualizar estado a APROBADO y publicar evento
            pedido.setEstado(EstadoPedido.APROBADO);
            pedidoRepository.save(pedido);

            PedidoAprobadoEventDTO event = new PedidoAprobadoEventDTO();
            event.setPedidoId(pedido.getId());
            event.setProductoId(pedido.getProductoId());
            event.setCantidad(pedido.getCantidad());
            eventPublisher.publicarPedidoAprobado(event);

            log.info("Pedido {} aprobado y evento publicado", pedido.getId());

        } catch (ResourceNotFoundException | DomainException e) {
            // Excepciones controladas (404 o 422) provenientes del ErrorDecoder
            pedido.setEstado(EstadoPedido.RECHAZADO);
            pedidoRepository.save(pedido);
            log.error("Falló la reserva de stock (error controlado) para pedido {}. Motivo: {}", pedido.getId(),
                    e.getMessage());
            throw e; // Relanzamos la misma excepción para que el manejador global devuelva el status
                     // correcto
        } catch (Exception e) {
            // Cualquier otro error (timeout, caída de inventario, etc.)
            pedido.setEstado(EstadoPedido.RECHAZADO);
            pedidoRepository.save(pedido);
            log.error("Falló la reserva de stock (error inesperado) para pedido {}. Motivo: {}", pedido.getId(),
                    e.getMessage());
            throw new DomainException("Error inesperado en la comunicación con inventario: " + e.getMessage());
        }

        return pedido;
    }
}