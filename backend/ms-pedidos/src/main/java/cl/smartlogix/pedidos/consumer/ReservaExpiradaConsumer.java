package cl.smartlogix.pedidos.consumer;

import cl.smartlogix.pedidos.client.InventarioClient;
import cl.smartlogix.pedidos.dto.event.ReservaExpiradaEvent;
import cl.smartlogix.pedidos.dto.request.CancelarReservaRequestDTO;
import cl.smartlogix.pedidos.entity.EstadoPedido;
import cl.smartlogix.pedidos.entity.Pedido;
import cl.smartlogix.pedidos.exception.ResourceNotFoundException;
import cl.smartlogix.pedidos.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static cl.smartlogix.pedidos.config.RabbitMQConfig.QUEUE_RESERVA_EXPIRADA;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReservaExpiradaConsumer {

    private final PedidoRepository pedidoRepository;
    private final InventarioClient inventarioClient;

    @RabbitListener(queues = QUEUE_RESERVA_EXPIRADA)
    @Transactional
    public void handleReservaExpirada(ReservaExpiradaEvent event, Message message) {
        String correlationId = (String) message.getMessageProperties().getHeader("X-Correlation-Id");
        if (correlationId != null) {
            MDC.put("correlationId", correlationId);
        }
        try {
            String reservaId = event.getReservaId();
            log.info("📥 Evento de reserva expirada recibido para reservaId: {}", reservaId);

            Long pedidoId = Long.parseLong(reservaId);
            Pedido pedido = pedidoRepository.findById(pedidoId)
                    .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con ID: " + pedidoId));

            if (pedido.getEstado() != EstadoPedido.PENDIENTE) {
                log.info("El pedido {} ya no está PENDIENTE (estado={}), se ignora expiración", pedidoId, pedido.getEstado());
                return;
            }

            pedido.setEstado(EstadoPedido.RECHAZADO);
            pedidoRepository.save(pedido);
            log.info("Pedido {} marcado como RECHAZADO por expiración de reserva", pedido.getNumeroOrden());

            List<CancelarReservaRequestDTO.ItemCancelacionDTO> items = pedido.getDetalles().stream()
                    .map(d -> new CancelarReservaRequestDTO.ItemCancelacionDTO(d.getProductoId(), d.getCantidad()))
                    .collect(Collectors.toList());
            CancelarReservaRequestDTO cancelRequest = new CancelarReservaRequestDTO(reservaId, items);

            inventarioClient.cancelarReserva(cancelRequest);
            log.info("Reserva cancelada en inventario para pedido {}", pedidoId);
        } catch (Exception e) {
            log.error("Error al procesar expiración de reserva: {}", e.getMessage());
        } finally {
            MDC.remove("correlationId");
        }
    }
}