package cl.smartlogix.pedidos.publisher;

import cl.smartlogix.pedidos.dto.event.PedidoAprobadoEventDTO;
import cl.smartlogix.pedidos.dto.event.PedidoRechazadoEventDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import static cl.smartlogix.pedidos.config.RabbitMQConfig.PEDIDO_EXCHANGE;
import static cl.smartlogix.pedidos.config.RabbitMQConfig.ROUTING_KEY_APROBADO;
import static cl.smartlogix.pedidos.config.RabbitMQConfig.ROUTING_KEY_RECHAZADO;

@Component
@RequiredArgsConstructor
@Slf4j
public class PedidoEventPublisher {
    private final RabbitTemplate rabbitTemplate;

    public void publicarPedidoAprobado(PedidoAprobadoEventDTO event) {
        rabbitTemplate.convertAndSend(PEDIDO_EXCHANGE, ROUTING_KEY_APROBADO, event);
        log.info("Evento PedidoAprobado publicado: {}", event);
    }

    public void publicarPedidoRechazado(PedidoRechazadoEventDTO event) {
        rabbitTemplate.convertAndSend(PEDIDO_EXCHANGE, ROUTING_KEY_RECHAZADO, event);
        log.warn("⚠️ Evento PedidoRechazado (Compensación de Stock) enviado a RabbitMQ: {}", event);
    }
}