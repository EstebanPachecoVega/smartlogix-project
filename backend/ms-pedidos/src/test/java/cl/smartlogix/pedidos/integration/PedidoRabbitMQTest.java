package cl.smartlogix.pedidos.integration;

import cl.smartlogix.pedidos.config.RabbitMQConfig;
import cl.smartlogix.pedidos.dto.event.EnvioActualizadoEventDTO;
import cl.smartlogix.pedidos.entity.EstadoPedido;
import cl.smartlogix.pedidos.entity.Pedido;
import cl.smartlogix.pedidos.repository.PedidoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Tag("docker")
class PedidoRabbitMQTest extends AbstractIntegrationTest {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private PedidoRepository pedidoRepository;

    @AfterEach
    void tearDown() {
        pedidoRepository.deleteAll();
    }

    @Test
    void consumirEventoEnvioActualizado_cambiaEstadoPedido() {
        Pedido pedido = Pedido.builder()
                .numeroOrden("ORD-RMQ-001")
                .estado(EstadoPedido.APROBADO)
                .usuarioId("user-rmq-001")
                .destinatario("Test")
                .calle("Calle")
                .numero("123")
                .comuna("Comuna")
                .ciudad("Ciudad")
                .totalCompra(5000)
                .fechaPedido(java.time.LocalDateTime.now())
                .build();
        pedido = pedidoRepository.save(pedido);
        final Long pedidoIdParaAssert = pedido.getId();

        EnvioActualizadoEventDTO evento = EnvioActualizadoEventDTO.builder()
                .pedidoId(pedidoIdParaAssert)
                .envioId(100L)
                .estadoEnvio("EN_TRANSITO")
                .build();

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ENVIO_EXCHANGE,
                RabbitMQConfig.ROUTING_KEY_ENVIO_ACTUALIZADO,
                evento
        );

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            Pedido pedidoActualizado = pedidoRepository.findById(pedidoIdParaAssert).orElseThrow();
            assertThat(pedidoActualizado.getEstado()).isEqualTo(EstadoPedido.EN_CAMINO);
        });
    }

    @Test
    void consumirEventoEnvioActualizado_entrega() {
        Pedido pedido = Pedido.builder()
                .numeroOrden("ORD-RMQ-002")
                .estado(EstadoPedido.EN_CAMINO)
                .usuarioId("user-rmq-002")
                .destinatario("Test")
                .calle("Calle")
                .numero("456")
                .comuna("Comuna")
                .ciudad("Ciudad")
                .totalCompra(3000)
                .fechaPedido(java.time.LocalDateTime.now())
                .build();
        pedido = pedidoRepository.save(pedido);
        final Long pedidoIdParaAssert2 = pedido.getId();

        EnvioActualizadoEventDTO evento = EnvioActualizadoEventDTO.builder()
                .pedidoId(pedidoIdParaAssert2)
                .envioId(200L)
                .estadoEnvio("ENTREGADO")
                .build();

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ENVIO_EXCHANGE,
                RabbitMQConfig.ROUTING_KEY_ENVIO_ACTUALIZADO,
                evento
        );

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            Pedido pedidoActualizado = pedidoRepository.findById(pedidoIdParaAssert2).orElseThrow();
            assertThat(pedidoActualizado.getEstado()).isEqualTo(EstadoPedido.ENTREGADO);
        });
    }
}
