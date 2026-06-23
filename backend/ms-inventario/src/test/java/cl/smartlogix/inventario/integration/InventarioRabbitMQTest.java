package cl.smartlogix.inventario.integration;

import cl.smartlogix.inventario.config.RabbitMQConfig;
import cl.smartlogix.inventario.dto.event.StockCompensacionEvent;
import cl.smartlogix.inventario.entity.Categoria;
import cl.smartlogix.inventario.entity.Producto;
import cl.smartlogix.inventario.repository.CategoriaRepository;
import cl.smartlogix.inventario.repository.ProductoRepository;
import cl.smartlogix.inventario.service.RedisStockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Tag("docker")
class InventarioRabbitMQTest extends AbstractIntegrationTest {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private RedisStockService redisStockService;

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
