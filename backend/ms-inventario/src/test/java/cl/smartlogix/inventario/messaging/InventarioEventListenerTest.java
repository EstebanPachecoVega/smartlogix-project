package cl.smartlogix.inventario.messaging;

import cl.smartlogix.inventario.dto.event.StockCompensacionEvent;
import cl.smartlogix.inventario.service.InventarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventarioEventListenerTest {

    @Mock
    private InventarioService inventarioService;

    private InventarioEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new InventarioEventListener(inventarioService);
    }

    @Test
    void procesarCompensacionStock_liberaStock() {
        var event = new StockCompensacionEvent(null, "ORD-001", 1L, 5, "res-123");

        listener.procesarCompensacionStock(event);

        verify(inventarioService).liberarStock(1L, 5, "res-123");
    }

    @Test
    void procesarCompensacionStock_throwsAmqpRejectWhenError() {
        var event = new StockCompensacionEvent(null, "ORD-001", 1L, 5, "res-123");
        doThrow(new RuntimeException("Error BD")).when(inventarioService).liberarStock(1L, 5, "res-123");

        assertThrows(AmqpRejectAndDontRequeueException.class, () -> listener.procesarCompensacionStock(event));
    }
}
