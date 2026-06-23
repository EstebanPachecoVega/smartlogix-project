package cl.smartlogix.envios.integration;

import cl.smartlogix.envios.config.RabbitMQConfig;
import cl.smartlogix.envios.dto.event.PedidoAprobadoEventDTO;
import cl.smartlogix.envios.entity.Envio;
import cl.smartlogix.envios.entity.EstadoEnvio;
import cl.smartlogix.envios.repository.EnvioRepository;
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
class EnvioRabbitMQTest extends AbstractIntegrationTest {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private EnvioRepository envioRepository;

    @AfterEach
    void tearDown() {
        envioRepository.deleteAll();
    }

    @Test
    void consumirPedidoAprobado_creaEnvio() {
        PedidoAprobadoEventDTO evento = PedidoAprobadoEventDTO.builder()
                .pedidoId(100L)
                .numeroOrden("ORD-RMQ-ENV-001")
                .usuarioId("user-rmq-env-001")
                .destinatario("Test Consumidor")
                .calle("Av. Rabbit")
                .numero("42")
                .comuna("Queue")
                .ciudad("Ciudad MQ")
                .metodoEnvio("DESPACHO")
                .build();

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_PEDIDOS,
                RabbitMQConfig.ROUTING_KEY_APROBADO,
                evento
        );

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            Envio envio = envioRepository.findByPedidoId(100L).orElse(null);
            assertThat(envio).isNotNull();
            assertThat(envio.getEstadoEnvio()).isEqualTo(EstadoEnvio.PENDIENTE);
            assertThat(envio.getNumeroTracking()).startsWith("TRK-");
            assertThat(envio.getEmpresaLogistica()).isEqualTo("LOGIX_CARRIER_INTEGRATION");
        });
    }

    @Test
    void consumirPedidoAprobado_duplicado_ignorado() {
        PedidoAprobadoEventDTO evento = PedidoAprobadoEventDTO.builder()
                .pedidoId(200L)
                .numeroOrden("ORD-RMQ-ENV-002")
                .usuarioId("user-rmq-env-002")
                .destinatario("Test Duplicado")
                .calle("Av. Rabbit")
                .numero("99")
                .comuna("Queue")
                .ciudad("Ciudad MQ")
                .metodoEnvio("DESPACHO")
                .build();

        Envio envioExistente = Envio.builder()
                .pedidoId(200L)
                .usuarioId("user-rmq-env-002")
                .destinatario("Test Duplicado")
                .calle("Av. Rabbit")
                .numero("99")
                .comuna("Queue")
                .ciudad("Ciudad MQ")
                .metodoEnvio("DESPACHO")
                .empresaLogistica("LOGIX_CARRIER")
                .numeroTracking("TRK-EXISTENTE")
                .estadoEnvio(EstadoEnvio.PENDIENTE)
                .build();
        envioRepository.save(envioExistente);

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_PEDIDOS,
                RabbitMQConfig.ROUTING_KEY_APROBADO,
                evento
        );

        await().atMost(10, TimeUnit.SECONDS).during(3, TimeUnit.SECONDS).untilAsserted(() -> {
            long count = envioRepository.findByPedidoId(200L).stream().count();
            assertThat(count).isEqualTo(1);
        });
    }
}
