package cl.smartlogix.pedidos.consumer;

import cl.smartlogix.pedidos.config.RabbitMQConfig;
import cl.smartlogix.pedidos.dto.event.EnvioActualizadoEventDTO;
import cl.smartlogix.pedidos.service.PedidoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EnvioActualizadoConsumer {

    private final PedidoService pedidoService;

    @RabbitListener(queues = RabbitMQConfig.PEDIDOS_ACTUALIZACIONES_QUEUE)
    public void handleEnvioActualizado(EnvioActualizadoEventDTO event) {
        log.info("📥 [EVENTO LOGÍSTICO] Detectado cambio en envío para Pedido ID: {} -> Estado: {}", 
                event.getPedidoId(), event.getEstadoEnvio());
        try {
            pedidoService.actualizarEstadoPorEnvio(event.getPedidoId(), event.getEstadoEnvio());
        } catch (Exception e) {
            log.error("❌ Error al sincronizar el estado del Pedido ID: {}. Causa: {}", 
                    event.getPedidoId(), e.getMessage());
        }
    }
}