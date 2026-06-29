package cl.smartlogix.pedidos.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String PEDIDO_EXCHANGE = "pedido.exchange";
    public static final String ROUTING_KEY_APROBADO = "pedido.aprobado";
    public static final String ROUTING_KEY_RECHAZADO = "pedido.rechazado";

    public static final String ENVIO_EXCHANGE = "envio.exchange";
    public static final String PEDIDOS_ACTUALIZACIONES_QUEUE = "pedidos.actualizaciones.queue";
    public static final String ROUTING_KEY_ENVIO_ACTUALIZADO = "envio.actualizado";

    public static final String EXCHANGE_RESERVA_EXPIRADA = "reserva.exchange";
    public static final String QUEUE_RESERVA_EXPIRADA = "pedidos.reserva.expirada.queue";
    public static final String ROUTING_KEY_RESERVA_EXPIRADA = "reserva.expirada";

    public static final String ENVIOS_QUEUE = "envios.queue";
    public static final String EXCHANGE_DLX = "envios.dlx";
    public static final String ROUTING_KEY_DLQ = "envios.rechazado.dlq";

    @Bean
    public TopicExchange pedidoExchange() {
        return new TopicExchange(PEDIDO_EXCHANGE);
    }

    @Bean
    public Queue enviosQueue() {
        return QueueBuilder.durable(ENVIOS_QUEUE)
                .withArgument("x-dead-letter-exchange", "envios.dlx")
                .withArgument("x-dead-letter-routing-key", "envios.rechazado.dlq")
                .build();
    }

    @Bean
    public Binding binding() {
        return BindingBuilder.bind(enviosQueue()).to(pedidoExchange()).with(ROUTING_KEY_APROBADO);
    }

    @Bean
    public TopicExchange envioExchange() {
        return new TopicExchange(ENVIO_EXCHANGE);
    }

    @Bean
    public Queue pedidosActualizacionesQueue() {
        return QueueBuilder.durable(PEDIDOS_ACTUALIZACIONES_QUEUE).build();
    }

    @Bean
    public Binding bindingActualizacionesPedido(Queue pedidosActualizacionesQueue, TopicExchange envioExchange) {
        return BindingBuilder.bind(pedidosActualizacionesQueue)
                .to(envioExchange)
                .with(ROUTING_KEY_ENVIO_ACTUALIZADO);
    }

    @Bean
    public Queue reservaExpiradaQueue() {
        return QueueBuilder.durable(QUEUE_RESERVA_EXPIRADA).build();
    }

    @Bean
    public TopicExchange reservaExpiradaExchange() {
        return new TopicExchange(EXCHANGE_RESERVA_EXPIRADA);
    }

    @Bean
    public Binding bindingReservaExpirada(Queue reservaExpiradaQueue, TopicExchange reservaExpiradaExchange) {
        return BindingBuilder.bind(reservaExpiradaQueue)
                .to(reservaExpiradaExchange)
                .with(ROUTING_KEY_RESERVA_EXPIRADA);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}