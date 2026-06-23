package cl.smartlogix.envios.publisher;

import cl.smartlogix.envios.config.RabbitMQConfig;
import cl.smartlogix.envios.dto.event.EnvioActualizadoEventDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.eq;

@ExtendWith(MockitoExtension.class)
class EnvioEventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Captor
    private ArgumentCaptor<EnvioActualizadoEventDTO> eventCaptor;

    private EnvioEventPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new EnvioEventPublisher(rabbitTemplate);
    }

    @Test
    void publicarEnvioActualizado_sendsEvent() {
        EnvioActualizadoEventDTO event = new EnvioActualizadoEventDTO();
        event.setPedidoId(1L);

        publisher.publicarEnvioActualizado(event);

        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE_ENVIOS),
                eq(RabbitMQConfig.ROUTING_KEY_ACTUALIZADO),
                eventCaptor.capture()
        );
        assertEquals(1L, eventCaptor.getValue().getPedidoId());
    }
}
