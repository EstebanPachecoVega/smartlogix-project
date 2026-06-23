package cl.smartlogix.inventario.integration;

import cl.smartlogix.inventario.entity.Categoria;
import cl.smartlogix.inventario.entity.Producto;
import cl.smartlogix.inventario.repository.CategoriaRepository;
import cl.smartlogix.inventario.repository.ProductoRepository;
import cl.smartlogix.inventario.service.RedisStockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Tag("docker")
class InventarioIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private RedisStockService redisStockService;

    private Producto producto;

    @BeforeEach
    void setUp() {
        Categoria categoria = new Categoria();
        categoria.setNombre("Test");
        categoria.setSlug("test");
        categoria = categoriaRepository.save(categoria);

        producto = Producto.builder()
                .sku("INT-SKU-001")
                .nombre("Producto Integración")
                .slug("producto-integracion")
                .precio(5000)
                .cantidad(50)
                .categoria(categoria)
                .build();
        producto = productoRepository.save(producto);

        redisStockService.inicializarStock(producto.getId(), producto.getCantidad());
    }

    @Test
    void flujoReservaConfirmar_ok() {
        Integer stockInicialRedis = redisStockService.obtenerStockRedis(producto.getId());
        assertThat(stockInicialRedis).isEqualTo(50);

        Boolean reservado = redisStockService.reservar("int-reserva-1", producto.getId(), 10, 10);
        assertThat(reservado).isTrue();

        Integer stockTrasReserva = redisStockService.obtenerStockRedis(producto.getId());
        assertThat(stockTrasReserva).isEqualTo(40);

        Integer cantidadReservada = redisStockService.confirmarReserva("int-reserva-1", producto.getId());
        assertThat(cantidadReservada).isEqualTo(10);
    }

    @Test
    void flujoReservaCancelar_restauraStock() {
        redisStockService.reservar("int-reserva-2", producto.getId(), 15, 10);

        Integer stockTrasReserva = redisStockService.obtenerStockRedis(producto.getId());
        assertThat(stockTrasReserva).isEqualTo(35);

        boolean cancelada = redisStockService.cancelarReserva("int-reserva-2", producto.getId());
        assertThat(cancelada).isTrue();

        Integer stockRestaurado = redisStockService.obtenerStockRedis(producto.getId());
        assertThat(stockRestaurado).isEqualTo(50);
    }

    @Test
    void reserva_stockInsuficiente_retornaFalse() {
        Boolean reservado = redisStockService.reservar("int-reserva-3", producto.getId(), 500, 10);
        assertThat(reservado).isFalse();
    }

    @Test
    void confirmarReserva_sinReserva_retornaNull() {
        Integer result = redisStockService.confirmarReserva("no-existe", producto.getId());
        assertThat(result).isNull();
    }

    @Test
    void obtenerStockRedis_productoSinInicializar_retornaNull() {
        Integer stock = redisStockService.obtenerStockRedis(9999L);
        assertThat(stock).isNull();
    }

    @Test
    void inicializarStock_y_eliminarStock() {
        redisStockService.inicializarStock(9998L, 200);
        Integer stock = redisStockService.obtenerStockRedis(9998L);
        assertThat(stock).isEqualTo(200);

        redisStockService.eliminarStock(9998L);
        stock = redisStockService.obtenerStockRedis(9998L);
        assertThat(stock).isNull();
    }
}
