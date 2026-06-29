package cl.smartlogix.pedidos.consumer;

import cl.smartlogix.pedidos.dto.event.EnvioActualizadoEventDTO;
import cl.smartlogix.pedidos.service.PedidoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnvioActualizadoConsumerTest {

    @Mock
    private PedidoService pedidoService;
    @Mock
    private Message message;

    private EnvioActualizadoConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new EnvioActualizadoConsumer(pedidoService);
    }

    @Test
    void handleEnvioActualizado_callsService() {
        MessageProperties props = new MessageProperties();
        props.setHeader("X-Correlation-Id", "corr-123");
        when(message.getMessageProperties()).thenReturn(props);
        EnvioActualizadoEventDTO event = new EnvioActualizadoEventDTO();
        event.setPedidoId(1L);
        event.setEstadoEnvio("EN_TRANSITO");

        consumer.handleEnvioActualizado(event, message);

        verify(pedidoService).actualizarEstadoPorEnvio(1L, "EN_TRANSITO");
    }

    @Test
    void handleEnvioActualizado_withoutCorrelationId_stillWorks() {
        MessageProperties props = new MessageProperties();
        when(message.getMessageProperties()).thenReturn(props);
        EnvioActualizadoEventDTO event = new EnvioActualizadoEventDTO();
        event.setPedidoId(2L);
        event.setEstadoEnvio("ENTREGADO");

        consumer.handleEnvioActualizado(event, message);

        verify(pedidoService).actualizarEstadoPorEnvio(2L, "ENTREGADO");
    }
}
