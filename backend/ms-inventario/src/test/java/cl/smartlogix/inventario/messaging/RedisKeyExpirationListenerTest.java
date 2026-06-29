package cl.smartlogix.inventario.messaging;

import cl.smartlogix.inventario.publisher.ReservaExpiradaPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisKeyExpirationListenerTest {

    @Mock
    private RedisMessageListenerContainer container;
    @Mock
    private ReservaExpiradaPublisher publisher;
    @Mock
    private Message message;

    private RedisKeyExpirationListener listener;

    @BeforeEach
    void setUp() {
        listener = new RedisKeyExpirationListener(container, publisher);
    }

    @Test
    void onMessage_reservaKey_publishesEvent() {
        when(message.toString()).thenReturn("reserva:res-123:1");

        listener.onMessage(message, new byte[0]);

        verify(publisher).publicarReservaExpirada("res-123");
    }

    @Test
    void onMessage_nonReservaKey_ignores() {
        when(message.toString()).thenReturn("stock:1");

        listener.onMessage(message, new byte[0]);

        verify(publisher, never()).publicarReservaExpirada(anyString());
    }

    @Test
    void onMessage_malformedKey_ignores() {
        when(message.toString()).thenReturn("reserva:onlyOnePart");

        listener.onMessage(message, new byte[0]);

        verify(publisher, never()).publicarReservaExpirada(anyString());
    }
}
