package cl.smartlogix.pedidos.config;

import org.slf4j.MDC;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
public class RabbitMQCorrelationIdConfig {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @PostConstruct
    public void enableCorrelationIdPropagation() {
        rabbitTemplate.setBeforePublishPostProcessors(message -> {
            String correlationId = MDC.get("correlationId");
            if (correlationId != null) {
                message.getMessageProperties().setHeader("X-Correlation-Id", correlationId);
            }
            return message;
        });
    }
}