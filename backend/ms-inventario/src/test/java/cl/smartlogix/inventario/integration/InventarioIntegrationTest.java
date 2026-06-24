package cl.smartlogix.inventario.integration;

import cl.smartlogix.inventario.config.RabbitMQConfig;
import cl.smartlogix.inventario.dto.event.StockCompensacionEvent;
import cl.smartlogix.inventario.entity.Categoria;
import cl.smartlogix.inventario.entity.Producto;
import cl.smartlogix.inventario.repository.CategoriaRepository;
import cl.smartlogix.inventario.repository.ProductoRepository;
import cl.smartlogix.inventario.service.RedisStockService;
import org.junit.jupiter.api.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Pruebas de integracion para ms-inventario.
 * <p>
 * Usan Testcontainers (MySQL, Redis, RabbitMQ) via {@link AbstractIntegrationTest}.
 * Se ejecutan con Docker activo y perfil "test".
 * <p>
 * Estructura:
 * <ul>
 *   <li>{@code RedisStock} - Flujo de reserva, confirmacion, cancelacion y gestion de stock en Redis</li>
 *   <li>{@code RabbitMQ} - Envio y consumo de evento de compensacion de stock</li>
 *   <li>{@code Concurrencia} - Reservas simultaneas y secuenciales con control de stock atomico</li>
 * </ul>
 */
@SpringBootTest
@ActiveProfiles("test")
@Tag("docker")
class InventarioIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    ProductoRepository productoRepository;

    @Autowired
    CategoriaRepository categoriaRepository;

    @Autowired
    RedisStockService redisStockService;

    @Autowired
    RabbitTemplate rabbitTemplate;

    // ---- Flujo de reserva, confirmacion y gestion de stock en Redis ----

    @Nested
    @Transactional
    class RedisStock {

        private Producto producto;

        @BeforeEach
        void setUp() {
            Categoria categoria = new Categoria();
            categoria.setNombre("Test");
            categoria.setSlug("test");
            categoria = categoriaRepository.save(categoria);

            producto = Producto.builder()
                    .sku("INT-SKU-001")
                    .nombre("Producto Integracion")
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

    // ---- Evento de compensacion via RabbitMQ ----

    @Nested
    @Transactional
    class RabbitMQ {

        private Producto producto;

        @BeforeEach
        void setUp() {
            Categoria cat = new Categoria();
            cat.setNombre("RabbitMQ");
            cat.setSlug("rabbitmq");
            cat = categoriaRepository.save(cat);

            producto = Producto.builder()
                    .sku("RMQ-SKU-001")
                    .nombre("Producto RabbitMQ")
                    .slug("producto-rabbitmq")
                    .precio(1000)
                    .cantidad(50)
                    .categoria(cat)
                    .build();
            producto = productoRepository.save(producto);

            redisStockService.inicializarStock(producto.getId(), 50);
        }

        @Test
        void enviarEventoCompensacion_liberaStock() {
            redisStockService.reservar("rmq-reserva", producto.getId(), 10, 10);
            Integer stockTrasReserva = redisStockService.obtenerStockRedis(producto.getId());
            assertThat(stockTrasReserva).isEqualTo(40);

            StockCompensacionEvent evento = new StockCompensacionEvent();
            evento.setProductoId(producto.getId());
            evento.setCantidad(10);
            evento.setReservaId("rmq-reserva");

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_PEDIDOS,
                    RabbitMQConfig.ROUTING_KEY_RECHAZADO,
                    evento
            );

            await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
                Integer stockActual = redisStockService.obtenerStockRedis(producto.getId());
                assertThat(stockActual).isEqualTo(50);
            });

            Producto productoBD = productoRepository.findById(producto.getId()).orElseThrow();
            assertThat(productoBD.getCantidad()).isEqualTo(50);
        }
    }

    // ---- Pruebas de concurrencia sobre stock atomico ----

    @Nested
    @Transactional
    class Concurrencia {

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
                    assertThat(resultado).as("Reserva " + i + " deberia ser exitosa").isTrue();
                } else {
                    assertThat(resultado).as("Tercera reserva debe fallar (stock=10, 4+4+4=12>10)").isFalse();
                }
            }

            Integer stockFinal = redisStockService.obtenerStockRedis(productoId);
            assertThat(stockFinal).isIn(2, 6, 10);
        }
    }
}
