package cl.smartlogix.inventario.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // --- INFRAESTRUCTURA PRINCIPAL ---
    public static final String QUEUE_LIBERAR_STOCK = "inventario.liberar.stock.queue";
    public static final String EXCHANGE_PEDIDOS = "pedido.exchange";
    public static final String ROUTING_KEY_RECHAZADO = "pedido.rechazado";

    // --- INFRAESTRUCTURA DLQ (Red de Seguridad) ---
    public static final String QUEUE_LIBERAR_STOCK_DLQ = "inventario.liberar.stock.dlq";
    public static final String EXCHANGE_DLX = "pedidos.dlx";
    public static final String ROUTING_KEY_DLQ = "pedido.rechazado.dlq";

    // 1. Declaramos la cola PRINCIPAL con sus parámetros de fallo hacia la DLQ
    @Bean
    public Queue liberarStockQueue() {
        return QueueBuilder.durable(QUEUE_LIBERAR_STOCK)
                .withArgument("x-dead-letter-exchange", EXCHANGE_DLX)
                .withArgument("x-dead-letter-routing-key", ROUTING_KEY_DLQ)
                .build();
    }

    // 2. Exchange Principal
    @Bean
    public TopicExchange pedidosExchange() {
        return new TopicExchange(EXCHANGE_PEDIDOS);
    }

    // 3. Binding Principal
    @Bean
    public Binding bindingLiberarStock(Queue liberarStockQueue, TopicExchange pedidosExchange) {
        return BindingBuilder.bind(liberarStockQueue).to(pedidosExchange).with(ROUTING_KEY_RECHAZADO);
    }

    // -----------------------------------------------------
    // CONFIGURACIÓN DE LA DEAD LETTER QUEUE (DLQ)
    // -----------------------------------------------------

    @Bean
    public TopicExchange deadLetterExchange() {
        return new TopicExchange(EXCHANGE_DLX);
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(QUEUE_LIBERAR_STOCK_DLQ).build();
    }

    @Bean
    public Binding deadLetterBinding(Queue deadLetterQueue, TopicExchange deadLetterExchange) {
        return BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange).with(ROUTING_KEY_DLQ);
    }

    // -----------------------------------------------------
    // CONFIGURACIÓN DEL CONVERSOR DE MENSAJES PARA JSON
    // -----------------------------------------------------
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