package cl.smartlogix.envios.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String ENVIOS_QUEUE = "envios.queue";

    @Bean
    public Queue enviosQueue() {
        return new Queue(ENVIOS_QUEUE, true);
    }
}