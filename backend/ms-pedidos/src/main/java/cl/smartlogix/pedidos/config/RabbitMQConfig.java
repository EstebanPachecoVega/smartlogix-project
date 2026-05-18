package cl.smartlogix.pedidos.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String PEDIDO_EXCHANGE = "pedido.exchange";
    public static final String ENVIOS_QUEUE = "envios.queue";

    public static final String ROUTING_KEY_APROBADO = "pedido.aprobado";
    public static final String ROUTING_KEY_RECHAZADO = "pedido.rechazado";

    @Bean
    public TopicExchange pedidoExchange() {
        return new TopicExchange(PEDIDO_EXCHANGE);
    }

    @Bean
    public Binding binding() {
        return new Binding(
                ENVIOS_QUEUE,
                Binding.DestinationType.QUEUE,
                PEDIDO_EXCHANGE,
                ROUTING_KEY_APROBADO,
                null
        );
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}