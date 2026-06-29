package cl.smartlogix.bff.controller;

import cl.smartlogix.bff.client.GatewayClient;
import cl.smartlogix.bff.config.TestSecurityConfig;
import cl.smartlogix.bff.dto.response.EnvioResponseDTO;
import cl.smartlogix.bff.dto.response.PagedResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@WebFluxTest(EnvioBffController.class)
@Import(TestSecurityConfig.class)
class EnvioBffControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private GatewayClient gatewayClient;

    @Test
    void listar_returnsEnvios() {
        PagedResponse<EnvioResponseDTO> paged = new PagedResponse<>();
        paged.setContent(List.of(new EnvioResponseDTO()));
        when(gatewayClient.listarEnvios(eq("mock-jwt"), any(), anyInt(), anyInt(), any()))
                .thenReturn(Mono.just(paged));

        webTestClient.get().uri("/bff/envios")
                .header("Authorization", "Bearer mock-jwt")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void obtener_returnsEnvio() {
        when(gatewayClient.obtenerEnvio(eq(1L), eq("mock-jwt"), any()))
                .thenReturn(Mono.just(new EnvioResponseDTO()));

        webTestClient.get().uri("/bff/envios/1")
                .header("Authorization", "Bearer mock-jwt")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void obtenerPorPedidoId_returnsEnvio() {
        when(gatewayClient.obtenerEnvioPorPedidoId(eq(1L), eq("mock-jwt"), any()))
                .thenReturn(Mono.just(new EnvioResponseDTO()));

        webTestClient.get().uri("/bff/envios/pedido/1")
                .header("Authorization", "Bearer mock-jwt")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void actualizarEstado_returnsEnvio() {
        when(gatewayClient.actualizarEstadoEnvio(eq(1L), eq("ENVIADO"), eq("mock-jwt"), any()))
                .thenReturn(Mono.just(new EnvioResponseDTO()));

        webTestClient.patch().uri("/bff/envios/1/estado?nuevoEstado=ENVIADO")
                .header("Authorization", "Bearer mock-jwt")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void obtenerPorTracking_returnsEnvio() {
        when(gatewayClient.obtenerEnvioPorTracking(eq("TRK-123"), eq("mock-jwt"), any()))
                .thenReturn(Mono.just(new EnvioResponseDTO()));

        webTestClient.get().uri("/bff/envios/tracking/TRK-123")
                .header("Authorization", "Bearer mock-jwt")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void listar_withInvalidAuth_returnsUnauthorized() {
        webTestClient.get().uri("/bff/envios")
                .header("Authorization", "InvalidToken")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void listar_withoutAuth_returnsUnauthorized() {
        webTestClient.get().uri("/bff/envios")
                .exchange()
                .expectStatus().isUnauthorized();
    }
}
