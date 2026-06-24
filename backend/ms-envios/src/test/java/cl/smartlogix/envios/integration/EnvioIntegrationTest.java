package cl.smartlogix.envios.integration;

import cl.smartlogix.envios.config.RabbitMQConfig;
import cl.smartlogix.envios.dto.event.PedidoAprobadoEventDTO;
import cl.smartlogix.envios.dto.response.EnvioResponseDTO;
import cl.smartlogix.envios.entity.Envio;
import cl.smartlogix.envios.entity.EstadoEnvio;
import cl.smartlogix.envios.repository.EnvioRepository;
import cl.smartlogix.envios.service.EnvioService;
import org.junit.jupiter.api.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Pruebas de integracion para ms-envios.
 * <p>
 * Usan Testcontainers (MySQL, RabbitMQ) via {@link AbstractIntegrationTest}.
 * Se ejecutan con Docker activo y perfil "test".
 * <p>
 * Estructura:
 * <ul>
 *   <li>{@code EnvioService} - Operaciones CRUD de envios via servicio</li>
 *   <li>{@code RabbitMQ} - Consumo de eventos de pedido aprobado via RabbitMQ (sin @Transactional para visibilidad cross-thread)</li>
 * </ul>
 */
@SpringBootTest
@ActiveProfiles("test")
@Tag("docker")
class EnvioIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    EnvioService envioService;

    @Autowired
    EnvioRepository envioRepository;

    @Autowired
    RabbitTemplate rabbitTemplate;

    // ---- Operaciones CRUD de Envio ----

    @Nested
    @Transactional
    class EnvioServiceOps {

        @Test
        void actualizarEstado_envioExistente() {
            Envio envio = Envio.builder()
                    .pedidoId(1L)
                    .usuarioId("user-001")
                    .destinatario("Juan Perez")
                    .calle("Av. Central")
                    .numero("789")
                    .comuna("Santiago")
                    .ciudad("Santiago")
                    .metodoEnvio("DESPACHO")
                    .empresaLogistica("TEST_CARRIER")
                    .numeroTracking("TRK-TEST-001")
                    .estadoEnvio(EstadoEnvio.PENDIENTE)
                    .build();
            envio = envioRepository.save(envio);

            EnvioResponseDTO result = envioService.actualizarEstado(envio.getId(), EstadoEnvio.ENVIADO);

            assertThat(result).isNotNull();
            Envio envioBD = envioRepository.findById(envio.getId()).orElseThrow();
            assertThat(envioBD.getEstadoEnvio()).isEqualTo(EstadoEnvio.ENVIADO);
        }

        @Test
        void listarTodos_retornaEnvios() {
            Envio envio1 = Envio.builder()
                    .pedidoId(10L).usuarioId("user-010")
                    .destinatario("A").calle("Calle 1").numero("1").comuna("Comuna 1").ciudad("Ciudad 1")
                    .metodoEnvio("DESPACHO").empresaLogistica("TEST").numeroTracking("TRK-LIST-001")
                    .estadoEnvio(EstadoEnvio.PENDIENTE)
                    .build();
            Envio envio2 = Envio.builder()
                    .pedidoId(20L).usuarioId("user-020")
                    .destinatario("B").calle("Calle 2").numero("2").comuna("Comuna 2").ciudad("Ciudad 2")
                    .metodoEnvio("DESPACHO").empresaLogistica("TEST").numeroTracking("TRK-LIST-002")
                    .estadoEnvio(EstadoEnvio.EN_TRANSITO)
                    .build();
            envioRepository.save(envio1);
            envioRepository.save(envio2);

            List<EnvioResponseDTO> result = envioService.listarTodos();

            assertThat(result).hasSize(2);
        }

        @Test
        void obtenerPorTracking_ok() {
            Envio envio = Envio.builder()
                    .pedidoId(30L).usuarioId("user-030")
                    .destinatario("C").calle("Calle 3").numero("3").comuna("Comuna 3").ciudad("Ciudad 3")
                    .metodoEnvio("DESPACHO").empresaLogistica("TEST").numeroTracking("TRK-FIND-001")
                    .estadoEnvio(EstadoEnvio.EN_REPARTO)
                    .build();
            envioRepository.save(envio);

            EnvioResponseDTO result = envioService.obtenerPorTracking("TRK-FIND-001");

            assertThat(result).isNotNull();
        }

        @Test
        void listarEnviosConProblemas_filtraCorrectamente() {
            Envio normal = Envio.builder()
                    .pedidoId(40L).usuarioId("user-040")
                    .destinatario("D").calle("Calle 4").numero("4").comuna("Comuna 4").ciudad("Ciudad 4")
                    .metodoEnvio("DESPACHO").empresaLogistica("TEST").numeroTracking("TRK-PROB-001")
                    .estadoEnvio(EstadoEnvio.EN_TRANSITO)
                    .build();
            Envio retrasado = Envio.builder()
                    .pedidoId(50L).usuarioId("user-050")
                    .destinatario("E").calle("Calle 5").numero("5").comuna("Comuna 5").ciudad("Ciudad 5")
                    .metodoEnvio("DESPACHO").empresaLogistica("TEST").numeroTracking("TRK-PROB-002")
                    .estadoEnvio(EstadoEnvio.RETRASADO)
                    .build();
            Envio devuelto = Envio.builder()
                    .pedidoId(60L).usuarioId("user-060")
                    .destinatario("F").calle("Calle 6").numero("6").comuna("Comuna 6").ciudad("Ciudad 6")
                    .metodoEnvio("DESPACHO").empresaLogistica("TEST").numeroTracking("TRK-PROB-003")
                    .estadoEnvio(EstadoEnvio.DEVUELTO)
                    .build();
            envioRepository.save(normal);
            envioRepository.save(retrasado);
            envioRepository.save(devuelto);

            List<EnvioResponseDTO> result = envioService.listarEnviosConProblemas();

            assertThat(result).hasSize(2);
        }
    }

    // ---- Eventos RabbitMQ (sin @Transactional para que el consumer vea los datos) ----

    @Nested
    class RabbitMQ {

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
}
