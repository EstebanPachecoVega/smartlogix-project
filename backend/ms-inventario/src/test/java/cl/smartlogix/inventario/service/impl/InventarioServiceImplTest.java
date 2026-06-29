package cl.smartlogix.inventario.service.impl;

import cl.smartlogix.inventario.dto.request.ReservarStockRequestDTO;
import cl.smartlogix.inventario.entity.Producto;
import cl.smartlogix.inventario.exception.DomainException;
import cl.smartlogix.inventario.exception.ResourceNotFoundException;
import cl.smartlogix.inventario.repository.ProductoRepository;
import cl.smartlogix.inventario.service.RedisStockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventarioServiceImplTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private RedisStockService redisStockService;

    private InventarioServiceImpl inventarioService;

    @BeforeEach
    void setUp() {
        inventarioService = new InventarioServiceImpl(productoRepository, redisStockService);
    }

    @Test
    void reservarStockLote_ok() {
        List<ReservarStockRequestDTO> items = List.of(
                new ReservarStockRequestDTO(1L, 5),
                new ReservarStockRequestDTO(2L, 3)
        );
        when(redisStockService.reservar(anyString(), eq(1L), eq(5), anyInt())).thenReturn(true);
        when(redisStockService.reservar(anyString(), eq(2L), eq(3), anyInt())).thenReturn(true);

        String reservaId = inventarioService.reservarStockLote(items, null);

        assertThat(reservaId).isNotBlank();
        verify(redisStockService, times(2)).reservar(anyString(), anyLong(), anyInt(), anyInt());
        verifyNoMoreInteractions(redisStockService);
    }

    @Test
    void reservarStockLote_conReservaIdBlank_generaUUID() {
        List<ReservarStockRequestDTO> items = List.of(
                new ReservarStockRequestDTO(1L, 5)
        );
        when(redisStockService.reservar(anyString(), eq(1L), eq(5), anyInt())).thenReturn(true);

        String reservaId = inventarioService.reservarStockLote(items, "");

        assertThat(reservaId).isNotBlank();
        assertThat(reservaId).doesNotContain("reserva-fija");
    }

    @Test
    void reservarStockLote_conReservaIdExistente() {
        List<ReservarStockRequestDTO> items = List.of(
                new ReservarStockRequestDTO(1L, 2)
        );
        when(redisStockService.reservar(eq("reserva-fija"), eq(1L), eq(2), anyInt())).thenReturn(true);

        String result = inventarioService.reservarStockLote(items, "reserva-fija");

        assertThat(result).isEqualTo("reserva-fija");
    }

    @Test
    void reservarStockLote_productoNoSincronizado_lanzaDomainException() {
        List<ReservarStockRequestDTO> items = List.of(
                new ReservarStockRequestDTO(1L, 5)
        );
        when(redisStockService.reservar(anyString(), eq(1L), eq(5), anyInt())).thenReturn(null);

        assertThatThrownBy(() -> inventarioService.reservarStockLote(items, null))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("no sincronizado");
    }

    @Test
    void reservarStockLote_stockInsuficiente_rollbackParcial() {
        List<ReservarStockRequestDTO> items = List.of(
                new ReservarStockRequestDTO(1L, 5),
                new ReservarStockRequestDTO(2L, 3)
        );
        when(redisStockService.reservar(anyString(), eq(1L), eq(5), anyInt())).thenReturn(true);
        when(redisStockService.reservar(anyString(), eq(2L), eq(3), anyInt())).thenReturn(false);

        assertThatThrownBy(() -> inventarioService.reservarStockLote(items, null))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Stock insuficiente");

        verify(redisStockService).cancelarReserva(anyString(), eq(1L));
        verify(redisStockService, never()).cancelarReserva(anyString(), eq(2L));
    }

    @Test
    void confirmarReserva_ok() {
        List<ReservarStockRequestDTO> items = List.of(
                new ReservarStockRequestDTO(1L, 5)
        );
        when(productoRepository.restarStockAtomico(1L, 5)).thenReturn(1);
        when(redisStockService.confirmarReserva("res-1", 1L)).thenReturn(5);

        inventarioService.confirmarReserva("res-1", items);

        verify(productoRepository).restarStockAtomico(1L, 5);
        verify(redisStockService).confirmarReserva("res-1", 1L);
    }

    @Test
    void confirmarReserva_falloDescuentoBD_lanzaDomainException() {
        List<ReservarStockRequestDTO> items = List.of(
                new ReservarStockRequestDTO(1L, 5)
        );
        when(productoRepository.restarStockAtomico(1L, 5)).thenReturn(0);

        assertThatThrownBy(() -> inventarioService.confirmarReserva("res-1", items))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("No se pudo descontar stock");

        verify(redisStockService, never()).confirmarReserva(anyString(), anyLong());
    }

    @Test
    void cancelarReserva_ok() {
        List<ReservarStockRequestDTO> items = List.of(
                new ReservarStockRequestDTO(1L, 5)
        );
        when(redisStockService.cancelarReserva("res-1", 1L)).thenReturn(true);

        inventarioService.cancelarReserva("res-1", items);
        verify(redisStockService).cancelarReserva("res-1", 1L);
    }

    @Test
    void cancelarReserva_noExiste_noLanzaExcepcion() {
        List<ReservarStockRequestDTO> items = List.of(
                new ReservarStockRequestDTO(1L, 5)
        );
        when(redisStockService.cancelarReserva("res-1", 1L)).thenReturn(false);

        inventarioService.cancelarReserva("res-1", items);
        verify(redisStockService).cancelarReserva("res-1", 1L);
    }

    @Test
    void confirmarReserva_redisNoExiste_logWarn() {
        List<ReservarStockRequestDTO> items = List.of(
                new ReservarStockRequestDTO(1L, 5)
        );
        when(productoRepository.restarStockAtomico(1L, 5)).thenReturn(1);
        when(redisStockService.confirmarReserva("res-1", 1L)).thenReturn(null);

        inventarioService.confirmarReserva("res-1", items);

        verify(productoRepository).restarStockAtomico(1L, 5);
        verify(redisStockService).confirmarReserva("res-1", 1L);
    }

    @Test
    void liberarStock_cantidadNull_lanzaDomainException() {
        assertThatThrownBy(() -> inventarioService.liberarStock(1L, null, "res-1"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("debe ser mayor a cero");
    }

    @Test
    void liberarStock_cantidadInvalida_lanzaDomainException() {
        assertThatThrownBy(() -> inventarioService.liberarStock(1L, -1, "res-1"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("debe ser mayor a cero");

        assertThatThrownBy(() -> inventarioService.liberarStock(1L, 0, "res-1"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("debe ser mayor a cero");
    }

    @Test
    void liberarStock_conReservaIdBlank_skipCancelar() {
        when(productoRepository.adicionarStockAtomico(1L, 10)).thenReturn(1);
        Producto producto = new Producto();
        producto.setId(1L);
        producto.setCantidad(100);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        inventarioService.liberarStock(1L, 10, "");

        verify(redisStockService, never()).cancelarReserva(anyString(), anyLong());
        verify(redisStockService).inicializarStock(1L, 100);
    }

    @Test
    void liberarStock_filas0_productoExiste_noLanzaExcepcion() {
        when(productoRepository.adicionarStockAtomico(1L, 10)).thenReturn(0);
        when(productoRepository.existsById(1L)).thenReturn(true);
        Producto producto = new Producto();
        producto.setId(1L);
        producto.setCantidad(50);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        inventarioService.liberarStock(1L, 10, null);

        verify(redisStockService).inicializarStock(1L, 50);
    }

    @Test
    void liberarStock_productoNoExiste_lanzaResourceNotFoundException() {
        when(productoRepository.adicionarStockAtomico(1L, 10)).thenReturn(0);
        when(productoRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> inventarioService.liberarStock(1L, 10, null))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Producto no existe");
    }

    @Test
    void liberarStock_ok_conReserva() {
        when(productoRepository.adicionarStockAtomico(1L, 10)).thenReturn(1);
        Producto producto = new Producto();
        producto.setId(1L);
        producto.setCantidad(100);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(redisStockService.cancelarReserva("res-1", 1L)).thenReturn(true);

        inventarioService.liberarStock(1L, 10, "res-1");

        verify(redisStockService).cancelarReserva("res-1", 1L);
        verify(redisStockService).inicializarStock(1L, 100);
    }

    @Test
    void liberarStock_ok_sinReserva() {
        when(productoRepository.adicionarStockAtomico(1L, 10)).thenReturn(1);
        Producto producto = new Producto();
        producto.setId(1L);
        producto.setCantidad(100);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        inventarioService.liberarStock(1L, 10, null);

        verify(redisStockService, never()).cancelarReserva(anyString(), anyLong());
        verify(redisStockService).inicializarStock(1L, 100);
    }

    @Test
    void sincronizarStockInicial_llamaInicializarParaCadaProducto() {
        Producto p1 = new Producto();
        p1.setId(1L);
        p1.setCantidad(10);
        Producto p2 = new Producto();
        p2.setId(2L);
        p2.setCantidad(20);
        when(productoRepository.findAll()).thenReturn(List.of(p1, p2));

        inventarioService.sincronizarStockInicial();

        verify(redisStockService).inicializarStock(1L, 10);
        verify(redisStockService).inicializarStock(2L, 20);
    }

    @Test
    void sincronizarStockInicial_sinProductos_noLlamaInicializar() {
        when(productoRepository.findAll()).thenReturn(List.of());

        inventarioService.sincronizarStockInicial();

        verify(redisStockService, never()).inicializarStock(anyLong(), anyInt());
    }

    @Test
    void reconciliarStock_corrigeDiscrepancia() {
        Producto p = new Producto();
        p.setId(1L);
        p.setCantidad(50);
        when(productoRepository.findAll()).thenReturn(List.of(p));
        when(redisStockService.obtenerStockRedis(1L)).thenReturn(30);

        inventarioService.reconciliarStock();

        verify(redisStockService).inicializarStock(1L, 50);
    }

    @Test
    void reconciliarStock_sinDiscrepancia_noCorrige() {
        Producto p = new Producto();
        p.setId(1L);
        p.setCantidad(50);
        when(productoRepository.findAll()).thenReturn(List.of(p));
        when(redisStockService.obtenerStockRedis(1L)).thenReturn(50);

        inventarioService.reconciliarStock();

        verify(redisStockService, never()).inicializarStock(anyLong(), anyInt());
    }

    @Test
    void reconciliarStock_stockRedisNull_inicializa() {
        Producto p = new Producto();
        p.setId(1L);
        p.setCantidad(50);
        when(productoRepository.findAll()).thenReturn(List.of(p));
        when(redisStockService.obtenerStockRedis(1L)).thenReturn(null);

        inventarioService.reconciliarStock();

        verify(redisStockService).inicializarStock(1L, 50);
    }
}
