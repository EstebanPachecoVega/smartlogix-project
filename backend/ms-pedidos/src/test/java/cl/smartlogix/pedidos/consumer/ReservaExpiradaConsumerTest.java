package cl.smartlogix.pedidos.consumer;

import cl.smartlogix.pedidos.client.InventarioClient;
import cl.smartlogix.pedidos.dto.event.ReservaExpiradaEvent;
import cl.smartlogix.pedidos.dto.request.CancelarReservaRequestDTO;
import cl.smartlogix.pedidos.entity.DetallePedido;
import cl.smartlogix.pedidos.entity.EstadoPedido;
import cl.smartlogix.pedidos.entity.Pedido;
import cl.smartlogix.pedidos.repository.PedidoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservaExpiradaConsumerTest {

    @Mock
    private PedidoRepository pedidoRepository;
    @Mock
    private InventarioClient inventarioClient;
    @Mock
    private Message message;

    @InjectMocks
    private ReservaExpiradaConsumer consumer;

    @Test
    void handleReservaExpirada_rechazaPedidoYCancelaReserva() {
        ReservaExpiradaEvent event = new ReservaExpiradaEvent("1");
        MessageProperties props = new MessageProperties();
        when(message.getMessageProperties()).thenReturn(props);

        Pedido pedido = Pedido.builder()
                .id(1L).numeroOrden("ORD-001").estado(EstadoPedido.PENDIENTE)
                .detalles(List.of(
                        DetallePedido.builder().productoId(10L).cantidad(2).build()
                ))
                .build();

        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        consumer.handleReservaExpirada(event, message);

        verify(pedidoRepository).save(argThat(p -> p.getEstado() == EstadoPedido.RECHAZADO));
        verify(inventarioClient).cancelarReserva(any(CancelarReservaRequestDTO.class));
    }

    @Test
    void handleReservaExpirada_pedidoNoPendiente_ignora() {
        ReservaExpiradaEvent event = new ReservaExpiradaEvent("1");
        MessageProperties props = new MessageProperties();
        when(message.getMessageProperties()).thenReturn(props);

        Pedido pedido = Pedido.builder()
                .id(1L).numeroOrden("ORD-001").estado(EstadoPedido.APROBADO)
                .build();

        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        consumer.handleReservaExpirada(event, message);

        verify(pedidoRepository, never()).save(any());
        verify(inventarioClient, never()).cancelarReserva(any());
    }

    @Test
    void handleReservaExpirada_pedidoNoEncontrado_manejaError() {
        ReservaExpiradaEvent event = new ReservaExpiradaEvent("999");
        MessageProperties props = new MessageProperties();
        when(message.getMessageProperties()).thenReturn(props);
        when(pedidoRepository.findById(999L)).thenReturn(Optional.empty());

        consumer.handleReservaExpirada(event, message);

        verify(pedidoRepository, never()).save(any());
        verify(inventarioClient, never()).cancelarReserva(any());
    }
}
