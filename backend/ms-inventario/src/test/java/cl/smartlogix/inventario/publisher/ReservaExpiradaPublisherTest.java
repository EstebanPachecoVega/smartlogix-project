package cl.smartlogix.inventario.publisher;

import cl.smartlogix.inventario.config.RabbitMQConfig;
import cl.smartlogix.inventario.dto.event.ReservaExpiradaEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReservaExpiradaPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Captor
    private ArgumentCaptor<ReservaExpiradaEvent> eventCaptor;

    private ReservaExpiradaPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new ReservaExpiradaPublisher(rabbitTemplate);
    }

    @Test
    void publicarReservaExpirada_sendsEventToRabbit() {
        publisher.publicarReservaExpirada("res-123");

        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE_RESERVA_EXPIRADA),
                eq(RabbitMQConfig.ROUTING_KEY_RESERVA_EXPIRADA),
                eventCaptor.capture()
        );
        assertEquals("res-123", eventCaptor.getValue().getReservaId());
    }
}
