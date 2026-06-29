package cl.smartlogix.pedidos.consumer;

import cl.smartlogix.pedidos.config.RabbitMQConfig;
import cl.smartlogix.pedidos.dto.event.EnvioActualizadoEventDTO;
import cl.smartlogix.pedidos.service.PedidoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EnvioActualizadoConsumer {

    private final PedidoService pedidoService;

    @RabbitListener(queues = RabbitMQConfig.PEDIDOS_ACTUALIZACIONES_QUEUE)
    public void handleEnvioActualizado(EnvioActualizadoEventDTO event, Message message) {
        String correlationId = (String) message.getMessageProperties().getHeader("X-Correlation-Id");
        if (correlationId != null) {
            MDC.put("correlationId", correlationId);
        }
        try {
            log.info("📥 [EVENTO LOGÍSTICO] Detectado cambio en envío para Pedido ID: {} -> Estado: {}", 
                    event.getPedidoId(), event.getEstadoEnvio());
            pedidoService.actualizarEstadoPorEnvio(event.getPedidoId(), event.getEstadoEnvio());
        } finally {
            MDC.remove("correlationId");
        }
    }
}