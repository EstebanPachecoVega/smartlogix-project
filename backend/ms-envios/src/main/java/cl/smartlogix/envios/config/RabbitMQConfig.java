package cl.smartlogix.envios.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String QUEUE_ENVIOS = "envios.queue";
    public static final String EXCHANGE_PEDIDOS = "pedido.exchange";
    public static final String ROUTING_KEY_APROBADO = "pedido.aprobado";

    public static final String EXCHANGE_ENVIOS = "envio.exchange";
    public static final String ROUTING_KEY_ACTUALIZADO = "envio.actualizado";

    public static final String QUEUE_ENVIOS_DLQ = "envios.dlq";
    public static final String EXCHANGE_DLX = "envios.dlx";
    public static final String ROUTING_KEY_DLQ = "envios.rechazado.dlq";

    @Bean
    public Queue enviosQueue() {
        return QueueBuilder.durable(QUEUE_ENVIOS)
                .withArgument("x-dead-letter-exchange", EXCHANGE_DLX)
                .withArgument("x-dead-letter-routing-key", ROUTING_KEY_DLQ)
                .build();
    }

    @Bean
    public TopicExchange pedidosExchange() {
        return new TopicExchange(EXCHANGE_PEDIDOS);
    }

    @Bean
    public Binding bindingEnvios(Queue enviosQueue, TopicExchange pedidosExchange) {
        return BindingBuilder.bind(enviosQueue).to(pedidosExchange).with(ROUTING_KEY_APROBADO);
    }

    @Bean
    public TopicExchange enviosExchange() {
        return new TopicExchange(EXCHANGE_ENVIOS);
    }

    @Bean
    public TopicExchange deadLetterExchange() {
        return new TopicExchange(EXCHANGE_DLX);
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(QUEUE_ENVIOS_DLQ).build();
    }

    @Bean
    public Binding deadLetterBinding(Queue deadLetterQueue, TopicExchange deadLetterExchange) {
        return BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange).with(ROUTING_KEY_DLQ);
    }

    @Bean
    public MessageConverter messageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        typeMapper.setTrustedPackages("*");
        typeMapper.setTypePrecedence(Jackson2JavaTypeMapper.TypePrecedence.INFERRED);
        converter.setJavaTypeMapper(typeMapper);
        return converter;
    }
}