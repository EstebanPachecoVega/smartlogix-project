package cl.smartlogix.envios.publisher;

import cl.smartlogix.envios.config.RabbitMQConfig;
import cl.smartlogix.envios.dto.event.EnvioActualizadoEventDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EnvioEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publicarEnvioActualizado(EnvioActualizadoEventDTO evento) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_ENVIOS,
                RabbitMQConfig.ROUTING_KEY_ACTUALIZADO,
                evento
        );
        log.info("📤 Evento EnvioActualizado publicado para Pedido ID: {}", evento.getPedidoId());
    }
}