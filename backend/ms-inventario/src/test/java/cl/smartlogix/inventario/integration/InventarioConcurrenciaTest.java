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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Tag("docker")
class InventarioConcurrenciaTest extends AbstractIntegrationTest {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private RedisStockService redisStockService;

    private Long productoId;

    @BeforeEach
    void setUp() {
        Categoria cat = new Categoria();
        cat.setNombre("Concurrencia");
        cat.setSlug("concurrencia");
        cat = categoriaRepository.save(cat);

        Producto p = Producto.builder()
                .sku("CONC-SKU-001")
                .nombre("Producto Concurrencia")
                .slug("producto-concurrencia")
                .precio(1000)
                .cantidad(10)
                .categoria(cat)
                .build();
        p = productoRepository.save(p);
        productoId = p.getId();

        redisStockService.inicializarStock(productoId, 10);
    }

    @Test
    void dosReservasSimultaneas_soloUnaExitosa() throws InterruptedException, ExecutionException {
        int stockPorReserva = 8;

        ExecutorService executor = Executors.newFixedThreadPool(2);
        Callable<Boolean> tarea = () -> redisStockService.reservar(
                Thread.currentThread().getName(),
                productoId,
                stockPorReserva,
                10
        );

        List<Future<Boolean>> futures = executor.invokeAll(List.of(tarea, tarea));
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        long exitosas = futures.stream().filter(f -> {
            try {
                return Boolean.TRUE.equals(f.get());
            } catch (Exception e) {
                return false;
            }
        }).count();

        assertThat(exitosas).as("Solo una reserva debe ser exitosa con stock=10 y dos solicitudes de 8 unidades")
                .isEqualTo(1);

        long fallidas = futures.stream().filter(f -> {
            try {
                return Boolean.FALSE.equals(f.get());
            } catch (Exception e) {
                return false;
            }
        }).count();
        assertThat(fallidas).isEqualTo(1);

        Integer stockFinal = redisStockService.obtenerStockRedis(productoId);
        assertThat(stockFinal).isIn(2, 10);
    }

    @Test
    void tresReservasSecuenciales_sumaNoExcedeStock() {
        for (int i = 0; i < 3; i++) {
            Boolean resultado = redisStockService.reservar("sec-" + i, productoId, 4, 10);
            if (i < 2) {
                assertThat(resultado).as("Reserva " + i + " debería ser exitosa").isTrue();
            } else {
                assertThat(resultado).as("Tercera reserva debe fallar (stock=10, 4+4+4=12>10)").isFalse();
            }
        }

        Integer stockFinal = redisStockService.obtenerStockRedis(productoId);
        assertThat(stockFinal).isIn(2, 6, 10);
    }
}
