package cl.smartlogix.pedidos.integration;

import cl.smartlogix.pedidos.config.RabbitMQConfig;
import cl.smartlogix.pedidos.dto.event.EnvioActualizadoEventDTO;
import cl.smartlogix.pedidos.dto.request.CrearPedidoRequestDTO;
import cl.smartlogix.pedidos.entity.EstadoPedido;
import cl.smartlogix.pedidos.entity.Pedido;
import cl.smartlogix.pedidos.repository.PedidoRepository;
import cl.smartlogix.pedidos.service.IdempotencyService;
import cl.smartlogix.pedidos.service.PedidoService;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import org.junit.jupiter.api.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import wiremock.org.eclipse.jetty.http.HttpStatus;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

/**
 * Pruebas de integracion para ms-pedidos.
 * <p>
 * Usan Testcontainers (MySQL, Redis, RabbitMQ) via {@link AbstractIntegrationTest}.
 * Se ejecutan con Docker activo y perfil "test".
 * <p>
 * Estructura:
 * <ul>
 *   <li>{@code SagaCompleta} - Flujo completo de creacion de pedido con WireMock simulando inventario</li>
 *   <li>{@code RabbitMQ} - Consumo de eventos de envio via RabbitMQ (sin @Transactional para visibilidad cross-thread)</li>
 * </ul>
 */
@SpringBootTest
@ActiveProfiles("test")
@Tag("docker")
class PedidoIntegrationTest extends AbstractIntegrationTest {

    static WireMockServer wireMockServer;

    @Autowired
    PedidoService pedidoService;

    @Autowired
    PedidoRepository pedidoRepository;

    @Autowired
    IdempotencyService idempotencyService;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @BeforeAll
    static void startWireMock() {
        wireMockServer = new WireMockServer(options()
                .port(18080)
                .extensions(new ReservaIdEchoTransformer()));
        wireMockServer.start();
        configureFor("localhost", 18080);
    }

    @AfterAll
    static void stopWireMock() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    CrearPedidoRequestDTO buildRequest() {
        CrearPedidoRequestDTO request = new CrearPedidoRequestDTO();
        request.setUsuarioId("user-int-001");
        request.setDestinatario("Maria Garcia");
        request.setCalle("Av. Los Pruebas");
        request.setNumero("456");
        request.setComuna("Providencia");
        request.setCiudad("Santiago");
        request.setMetodoEnvio("DESPACHO");
        request.setPlataforma("DESKTOP");

        CrearPedidoRequestDTO.DetalleRequestDTO item = new CrearPedidoRequestDTO.DetalleRequestDTO();
        item.setProductoId(1L);
        item.setSku("INT-SKU-001");
        item.setNombreProducto("Producto Integracion");
        item.setPrecioUnitario(5000);
        item.setCantidad(2);
        request.setItems(List.of(item));

        return request;
    }

    // ---- Saga + Idempotencia (WireMock) ----

    @Nested
    @Transactional
    class SagaCompleta {

        @Test
        void crearPedido_sagaCompleta_conWireMock() {
            stubFor(post(urlEqualTo("/api/inventario/reservar"))
                    .willReturn(aResponse()
                            .withStatus(HttpStatus.OK_200)
                            .withHeader("Content-Type", "application/json")
                            .withTransformers("reserva-id-echo")));

            stubFor(post(urlEqualTo("/api/inventario/confirmar"))
                    .willReturn(aResponse()
                            .withStatus(HttpStatus.OK_200)));

            Pedido pedido = pedidoService.crearPedido(buildRequest(), "user-int-001", null);

            assertThat(pedido).isNotNull();
            assertThat(pedido.getEstado()).isEqualTo(EstadoPedido.APROBADO);
            assertThat(pedido.getNumeroOrden()).startsWith("ORD-");
            assertThat(pedido.getTotalCompra()).isEqualTo(10000);

            Pedido pedidoBD = pedidoRepository.findById(pedido.getId()).orElseThrow();
            assertThat(pedidoBD.getEstado()).isEqualTo(EstadoPedido.APROBADO);
        }

        @Test
        void idempotencyService_funciona() {
            String key = "idem-int-key-001";
            assertThat(idempotencyService.isProcessed(key)).isFalse();

            idempotencyService.markProcessed(key);
            assertThat(idempotencyService.isProcessed(key)).isTrue();
        }

        @Test
        void idempotencyKeyDuplicada_rechazaPedido() {
            String idempotencyKey = "idem-int-key-002";

            stubFor(post(urlEqualTo("/api/inventario/reservar"))
                    .willReturn(aResponse()
                            .withStatus(HttpStatus.OK_200)
                            .withHeader("Content-Type", "application/json")
                            .withTransformers("reserva-id-echo")));

            stubFor(post(urlEqualTo("/api/inventario/confirmar"))
                    .willReturn(aResponse()
                            .withStatus(HttpStatus.OK_200)));

            pedidoService.crearPedido(buildRequest(), "user-int-003", idempotencyKey);

            assertThatThrownBy(() -> pedidoService.crearPedido(buildRequest(), "user-int-003", idempotencyKey))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("idempotencia");
        }
    }

    // ---- Eventos RabbitMQ (sin @Transactional para que el consumer vea los datos) ----

    @Nested
    class RabbitMQ {

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

    public static class ReservaIdEchoTransformer extends ResponseDefinitionTransformer {

        @Override
        public ResponseDefinition transform(Request request, ResponseDefinition responseDefinition, FileSource files, Parameters parameters) {
            String body = new String(request.getBody(), StandardCharsets.UTF_8);
            int idx = body.indexOf("\"reservaId\"");
            if (idx == -1) {
                return aResponse().withStatus(500).build();
            }
            int colonIdx = body.indexOf(':', idx);
            int quoteStart = body.indexOf('"', colonIdx + 1);
            int quoteEnd = body.indexOf('"', quoteStart + 1);
            String reservaId = body.substring(quoteStart + 1, quoteEnd);
            String json = "{\"reservaId\": \"" + reservaId + "\"}";
            return aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(json)
                    .build();
        }

        @Override
        public String getName() {
            return "reserva-id-echo";
        }
    }
}
