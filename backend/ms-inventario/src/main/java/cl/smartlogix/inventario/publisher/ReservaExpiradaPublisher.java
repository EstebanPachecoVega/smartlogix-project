package cl.smartlogix.inventario.publisher;

import cl.smartlogix.inventario.config.RabbitMQConfig;
import cl.smartlogix.inventario.dto.event.ReservaExpiradaEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReservaExpiradaPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publicarReservaExpirada(String reservaId) {
        ReservaExpiradaEvent event = new ReservaExpiradaEvent(reservaId);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_RESERVA_EXPIRADA,
                RabbitMQConfig.ROUTING_KEY_RESERVA_EXPIRADA,
                event
        );
        log.info("Evento de reserva expirada publicado para reservaId: {}", reservaId);
    }
}