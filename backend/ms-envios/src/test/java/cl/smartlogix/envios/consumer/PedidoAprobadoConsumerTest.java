package cl.smartlogix.envios.consumer;

import cl.smartlogix.envios.dto.event.PedidoAprobadoEventDTO;
import cl.smartlogix.envios.entity.Envio;
import cl.smartlogix.envios.repository.EnvioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoAprobadoConsumerTest {

    @Mock
    private EnvioRepository envioRepository;
    @Mock
    private Message message;

    @InjectMocks
    private PedidoAprobadoConsumer consumer;

    @Test
    void handlePedidoAprobado_createsEnvio() {
        MessageProperties props = new MessageProperties();
        when(message.getMessageProperties()).thenReturn(props);
        when(envioRepository.findByPedidoId(1L)).thenReturn(Optional.empty());

        var event = PedidoAprobadoEventDTO.builder()
                .pedidoId(1L).usuarioId("user-1").destinatario("Juan")
                .calle("Av 1").numero("123").comuna("Santiago").ciudad("Santiago")
                .codigoPostal("8320000").metodoEnvio("ESTANDAR").pesoKg(1.0).dimensiones("10x10")
                .build();

        consumer.handlePedidoAprobado(event, message);

        ArgumentCaptor<Envio> captor = ArgumentCaptor.forClass(Envio.class);
        verify(envioRepository).save(captor.capture());
        Envio saved = captor.getValue();
        assertEquals(1L, saved.getPedidoId());
        assertEquals("user-1", saved.getUsuarioId());
        assertEquals("Juan", saved.getDestinatario());
        assertNotNull(saved.getNumeroTracking());
        assertTrue(saved.getNumeroTracking().startsWith("TRK-"));
    }

    @Test
    void handlePedidoAprobado_duplicate_ignored() {
        MessageProperties props = new MessageProperties();
        when(message.getMessageProperties()).thenReturn(props);
        when(envioRepository.findByPedidoId(1L)).thenReturn(Optional.of(Envio.builder().build()));

        var event = PedidoAprobadoEventDTO.builder().pedidoId(1L).build();

        consumer.handlePedidoAprobado(event, message);

        verify(envioRepository, never()).save(any());
    }

    @Test
    void handlePedidoAprobado_setsCorrelationIdInMdc() {
        MessageProperties props = new MessageProperties();
        props.setHeader("X-Correlation-Id", "test-correlation-id");
        when(message.getMessageProperties()).thenReturn(props);
        when(envioRepository.findByPedidoId(1L)).thenReturn(Optional.empty());

        var event = PedidoAprobadoEventDTO.builder()
                .pedidoId(1L).usuarioId("user-1").destinatario("Juan")
                .calle("Av 1").numero("123").comuna("Santiago").ciudad("Santiago")
                .codigoPostal("8320000").metodoEnvio("ESTANDAR").pesoKg(1.0).dimensiones("10x10")
                .build();

        doAnswer(invocation -> {
            assertEquals("test-correlation-id", MDC.get("correlationId"));
            return null;
        }).when(envioRepository).save(any());

        consumer.handlePedidoAprobado(event, message);

        assertNull(MDC.get("correlationId"));
    }

    @Test
    void handlePedidoAprobado_rethrowsOnException() {
        MessageProperties props = new MessageProperties();
        when(message.getMessageProperties()).thenReturn(props);
        when(envioRepository.findByPedidoId(1L)).thenReturn(Optional.empty());
        when(envioRepository.save(any())).thenThrow(new RuntimeException("DB error"));

        var event = PedidoAprobadoEventDTO.builder()
                .pedidoId(1L).usuarioId("user-1").destinatario("Juan")
                .calle("Av 1").numero("123").comuna("Santiago").ciudad("Santiago")
                .codigoPostal("8320000").metodoEnvio("ESTANDAR").pesoKg(1.0).dimensiones("10x10")
                .build();

        assertThrows(AmqpRejectAndDontRequeueException.class,
                () -> consumer.handlePedidoAprobado(event, message));
    }
}
