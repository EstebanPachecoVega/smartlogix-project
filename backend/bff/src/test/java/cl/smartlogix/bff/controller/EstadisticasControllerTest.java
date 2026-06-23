package cl.smartlogix.bff.controller;

import cl.smartlogix.bff.client.GatewayClient;
import cl.smartlogix.bff.config.TestSecurityConfig;
import cl.smartlogix.bff.dto.response.*;
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

@WebFluxTest(EstadisticasController.class)
@Import(TestSecurityConfig.class)
class EstadisticasControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private GatewayClient gatewayClient;

    @Test
    void getVentasPlataforma_returnsData() {
        when(gatewayClient.getVentasPlataforma(eq("mock-jwt"), any()))
                .thenReturn(Mono.just(List.of(new VentasPlataformaResponseDTO())));

        webTestClient.get().uri("/bff/estadisticas/ventas-plataforma")
                .header("Authorization", "Bearer mock-jwt")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void getComparacionAnual_returnsData() {
        when(gatewayClient.getComparacionAnual(eq("mock-jwt"), any()))
                .thenReturn(Mono.just(List.of(new ComparacionAnualResponseDTO())));

        webTestClient.get().uri("/bff/estadisticas/comparacion-anual")
                .header("Authorization", "Bearer mock-jwt")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void getVentasPorCategoria_returnsData() {
        when(gatewayClient.listarCategorias(eq("mock-jwt"), any()))
                .thenReturn(Mono.just(List.of(
                    new CategoriaResponseDTO(1L, "Electrónicos", "electronica", null, 1L, null, 1, true, null, null)
                )));
        when(gatewayClient.getCantidadPorProducto(eq("mock-jwt"), any()))
                .thenReturn(Mono.just(List.of()));
        when(gatewayClient.getMapaCategorias(eq("mock-jwt"), any()))
                .thenReturn(Mono.just(List.of()));

        webTestClient.get().uri("/bff/estadisticas/ventas-por-categoria")
                .header("Authorization", "Bearer mock-jwt")
                .exchange()
                .expectStatus().isOk();
    }

}
