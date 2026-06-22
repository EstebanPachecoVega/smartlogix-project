package cl.smartlogix.pedidos.service.impl;

import cl.smartlogix.pedidos.client.InventarioClient;
import cl.smartlogix.pedidos.dto.event.PedidoAprobadoEventDTO;
import cl.smartlogix.pedidos.dto.event.PedidoRechazadoEventDTO;
import cl.smartlogix.pedidos.dto.request.CrearPedidoRequestDTO;
import cl.smartlogix.pedidos.dto.request.PedidoStockRequestDTO;
import cl.smartlogix.pedidos.dto.response.PedidoStockResponseDTO;
import cl.smartlogix.pedidos.entity.DetallePedido;
import cl.smartlogix.pedidos.entity.EstadoPedido;
import cl.smartlogix.pedidos.entity.Pedido;
import cl.smartlogix.pedidos.exception.DomainException;
import cl.smartlogix.pedidos.exception.ResourceNotFoundException;
import cl.smartlogix.pedidos.publisher.PedidoEventPublisher;
import cl.smartlogix.pedidos.repository.PedidoRepository;
import cl.smartlogix.pedidos.service.IdempotencyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoServiceImplTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private InventarioClient inventarioClient;

    @Mock
    private PedidoEventPublisher eventPublisher;

    @Mock
    private IdempotencyService idempotencyService;

    @Captor
    private ArgumentCaptor<Pedido> pedidoCaptor;

    @Captor
    private ArgumentCaptor<PedidoAprobadoEventDTO> aprobadoCaptor;

    @Captor
    private ArgumentCaptor<PedidoRechazadoEventDTO> rechazadoCaptor;

    private PedidoServiceImpl pedidoService;

    @BeforeEach
    void setUp() {
        pedidoService = new PedidoServiceImpl(pedidoRepository, inventarioClient, eventPublisher, idempotencyService);
    }

    private CrearPedidoRequestDTO buildRequest() {
        CrearPedidoRequestDTO request = new CrearPedidoRequestDTO();
        request.setUsuarioId("user-123");
        request.setDestinatario("Juan Pérez");
        request.setCalle("Av. Principal");
        request.setNumero("123");
        request.setComuna("Santiago");
        request.setCiudad("Santiago");
        request.setMetodoEnvio("DESPACHO");
        request.setPlataforma("DESKTOP");

        CrearPedidoRequestDTO.DetalleRequestDTO item = new CrearPedidoRequestDTO.DetalleRequestDTO();
        item.setProductoId(1L);
        item.setSku("SKU-001");
        item.setNombreProducto("Producto 1");
        item.setPrecioUnitario(1000);
        item.setCantidad(2);
        request.setItems(List.of(item));

        return request;
    }

    @Test
    void crearPedido_sagaCompleta() {
        CrearPedidoRequestDTO request = buildRequest();
        String usuarioId = "user-123";
        String idempotencyKey = "idem-001";

        when(idempotencyService.isProcessed("idem-001")).thenReturn(false);

        when(inventarioClient.reservarStock(any(PedidoStockRequestDTO.class))).thenAnswer(invocation -> {
            PedidoStockResponseDTO resp = new PedidoStockResponseDTO();
            resp.setReservaId(invocation.<PedidoStockRequestDTO>getArgument(0).getReservaId());
            return resp;
        });
        doNothing().when(inventarioClient).confirmarReserva(any());

        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> {
            Pedido p = invocation.getArgument(0);
            if (p.getId() == null) p.setId(1L);
            return p;
        });

        Pedido result = pedidoService.crearPedido(request, usuarioId, idempotencyKey);

        assertThat(result.getEstado()).isEqualTo(EstadoPedido.APROBADO);
        assertThat(result.getNumeroOrden()).startsWith("ORD-");
        assertThat(result.getTotalCompra()).isEqualTo(2000);
        assertThat(result.getDetalles()).hasSize(1);

        verify(pedidoRepository, times(2)).save(any(Pedido.class));
        verify(eventPublisher).publicarPedidoAprobado(any(PedidoAprobadoEventDTO.class));
        verify(idempotencyService).markProcessed("idem-001");
    }

    @Test
    void crearPedido_idempotenciaKeyYaProcesada_lanzaDomainException() {
        when(idempotencyService.isProcessed("idem-duplicado")).thenReturn(true);

        assertThatThrownBy(() -> pedidoService.crearPedido(buildRequest(), "user-123", "idem-duplicado"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("ya fue procesada");

        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void crearPedido_falloReservaStock_pedidoRechazado() {
        CrearPedidoRequestDTO request = buildRequest();
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> {
            Pedido p = invocation.getArgument(0);
            if (p.getId() == null) p.setId(1L);
            return p;
        });
        when(inventarioClient.reservarStock(any(PedidoStockRequestDTO.class)))
                .thenThrow(new RuntimeException("Stock insuficiente"));

        assertThatThrownBy(() -> pedidoService.crearPedido(request, "user-123", null))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("No se pudo reservar stock");

        verify(pedidoRepository, times(2)).save(pedidoCaptor.capture());
        Pedido pedidoFinal = pedidoCaptor.getAllValues().get(1);
        assertThat(pedidoFinal.getEstado()).isEqualTo(EstadoPedido.RECHAZADO);
        verify(eventPublisher, never()).publicarPedidoAprobado(any());
    }

    @Test
    void crearPedido_falloConfirmacionReserva_publicaCompensacion() {
        CrearPedidoRequestDTO request = buildRequest();
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> {
            Pedido p = invocation.getArgument(0);
            if (p.getId() == null) p.setId(1L);
            return p;
        });
        when(inventarioClient.reservarStock(any(PedidoStockRequestDTO.class))).thenAnswer(invocation -> {
            PedidoStockResponseDTO resp = new PedidoStockResponseDTO();
            resp.setReservaId(invocation.<PedidoStockRequestDTO>getArgument(0).getReservaId());
            return resp;
        });
        doThrow(new RuntimeException("Error confirmando")).when(inventarioClient).confirmarReserva(any());

        assertThatThrownBy(() -> pedidoService.crearPedido(request, "user-123", null))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("No se pudo confirmar");

        verify(eventPublisher).publicarPedidoRechazado(any(PedidoRechazadoEventDTO.class));
        verify(pedidoRepository, times(2)).save(pedidoCaptor.capture());
        Pedido pedidoFinal = pedidoCaptor.getAllValues().get(1);
        assertThat(pedidoFinal.getEstado()).isEqualTo(EstadoPedido.RECHAZADO);
    }

    @Test
    void crearPedido_reservaIdNoCoincide_lanzaDomainException() {
        CrearPedidoRequestDTO request = buildRequest();
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> {
            Pedido p = invocation.getArgument(0);
            if (p.getId() == null) p.setId(1L);
            return p;
        });
        when(inventarioClient.reservarStock(any(PedidoStockRequestDTO.class))).thenAnswer(invocation -> {
            PedidoStockResponseDTO resp = new PedidoStockResponseDTO();
            resp.setReservaId("otro-id");
            return resp;
        });

        assertThatThrownBy(() -> pedidoService.crearPedido(request, "user-123", null))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("no coincide");
    }

    @Test
    void obtenerPedidoPorId_ok() {
        Pedido pedido = new Pedido();
        pedido.setId(1L);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        Pedido result = pedidoService.obtenerPedidoPorId(1L);

        assertThat(result).isEqualTo(pedido);
    }

    @Test
    void obtenerPedidoPorId_noExiste_lanzaResourceNotFoundException() {
        when(pedidoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pedidoService.obtenerPedidoPorId(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void obtenerPedidoPorNumeroOrden_noExiste_lanzaResourceNotFoundException() {
        when(pedidoRepository.findByNumeroOrden("NO-EXISTE")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pedidoService.obtenerPedidoPorNumeroOrden("NO-EXISTE"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void listarPedidosPorUsuario_retornaLista() {
        Pedido p = new Pedido();
        when(pedidoRepository.findByUsuarioId("user-123")).thenReturn(List.of(p));

        List<Pedido> result = pedidoService.listarPedidosPorUsuario("user-123");

        assertThat(result).hasSize(1);
    }

    @Test
    void listarPedidos_retornaLista() {
        when(pedidoRepository.findAll()).thenReturn(List.of(new Pedido(), new Pedido()));

        List<Pedido> result = pedidoService.listarPedidos();

        assertThat(result).hasSize(2);
    }

    @Test
    void actualizarEstadoPorEnvio_transicionesCompletas() {
        Pedido pedido = new Pedido();
        pedido.setId(1L);
        pedido.setEstado(EstadoPedido.APROBADO);
        pedido.setNumeroOrden("ORD-001");
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        pedidoService.actualizarEstadoPorEnvio(1L, "EN_TRANSITO");
        assertThat(pedido.getEstado()).isEqualTo(EstadoPedido.EN_CAMINO);
        verify(pedidoRepository).save(pedido);
    }

    @Test
    void actualizarEstadoPorEnvio_entregado() {
        Pedido pedido = new Pedido();
        pedido.setId(1L);
        pedido.setEstado(EstadoPedido.EN_CAMINO);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        pedidoService.actualizarEstadoPorEnvio(1L, "ENTREGADO");

        assertThat(pedido.getEstado()).isEqualTo(EstadoPedido.ENTREGADO);
    }

    @Test
    void actualizarEstadoPorEnvio_devuelto_rechazado() {
        Pedido pedido = new Pedido();
        pedido.setId(1L);
        pedido.setEstado(EstadoPedido.EN_CAMINO);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        pedidoService.actualizarEstadoPorEnvio(1L, "DEVUELTO");

        assertThat(pedido.getEstado()).isEqualTo(EstadoPedido.RECHAZADO);
    }

    @Test
    void actualizarEstadoPorEnvio_mismoEstado_noGuarda() {
        Pedido pedido = new Pedido();
        pedido.setId(1L);
        pedido.setEstado(EstadoPedido.APROBADO);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        pedidoService.actualizarEstadoPorEnvio(1L, "PREPARANDO");

        assertThat(pedido.getEstado()).isEqualTo(EstadoPedido.APROBADO);
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void actualizarEstadoPorEnvio_pedidoNoExiste_lanzaResourceNotFoundException() {
        when(pedidoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pedidoService.actualizarEstadoPorEnvio(999L, "ENVIADO"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void actualizarEstadoPorEnvio_estadoDesconocido_noCambia() {
        Pedido pedido = new Pedido();
        pedido.setId(1L);
        pedido.setEstado(EstadoPedido.PENDIENTE);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        pedidoService.actualizarEstadoPorEnvio(1L, "ESTADO_INEXISTENTE");

        assertThat(pedido.getEstado()).isEqualTo(EstadoPedido.PENDIENTE);
        verify(pedidoRepository, never()).save(any());
    }
}
