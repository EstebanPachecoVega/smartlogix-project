package cl.smartlogix.pedidos.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // --- INFRAESTRUCTURA DE EMISIÓN (PEDIDOS -> ENVÍOS) ---
    public static final String PEDIDO_EXCHANGE = "pedido.exchange";
    public static final String ENVIOS_QUEUE = "envios.queue";
    public static final String ROUTING_KEY_APROBADO = "pedido.aprobado";
    public static final String ROUTING_KEY_RECHAZADO = "pedido.rechazado";

    // --- INFRAESTRUCTURA DE RECEPCIÓN (ENVÍOS -> PEDIDOS) ---
    public static final String ENVIO_EXCHANGE = "envio.exchange";
    public static final String PEDIDOS_ACTUALIZACIONES_QUEUE = "pedidos.actualizaciones.queue";
    public static final String ROUTING_KEY_ENVIO_ACTUALIZADO = "envio.actualizado";

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
                null);
    }

    // Binding para capturar las novedades de ms-envios y actualizar el estado del pedido
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
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}