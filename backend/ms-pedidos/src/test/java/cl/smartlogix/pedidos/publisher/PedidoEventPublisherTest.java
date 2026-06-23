package cl.smartlogix.pedidos.publisher;

import cl.smartlogix.pedidos.dto.event.PedidoAprobadoEventDTO;
import cl.smartlogix.pedidos.dto.event.PedidoRechazadoEventDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static cl.smartlogix.pedidos.config.RabbitMQConfig.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PedidoEventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Captor
    private ArgumentCaptor<PedidoAprobadoEventDTO> aprobadoCaptor;
    @Captor
    private ArgumentCaptor<PedidoRechazadoEventDTO> rechazadoCaptor;

    private PedidoEventPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new PedidoEventPublisher(rabbitTemplate);
    }

    @Test
    void publicarPedidoAprobado_sendsEvent() {
        var event = PedidoAprobadoEventDTO.builder().numeroOrden("ORD-001").build();

        publisher.publicarPedidoAprobado(event);

        verify(rabbitTemplate).convertAndSend(eq(PEDIDO_EXCHANGE), eq(ROUTING_KEY_APROBADO), aprobadoCaptor.capture());
        assertEquals("ORD-001", aprobadoCaptor.getValue().getNumeroOrden());
    }

    @Test
    void publicarPedidoRechazado_sendsEvent() {
        var event = PedidoRechazadoEventDTO.builder().productoId(1L).build();

        publisher.publicarPedidoRechazado(event);

        verify(rabbitTemplate).convertAndSend(eq(PEDIDO_EXCHANGE), eq(ROUTING_KEY_RECHAZADO), rechazadoCaptor.capture());
        assertEquals(1L, rechazadoCaptor.getValue().getProductoId());
    }
}
