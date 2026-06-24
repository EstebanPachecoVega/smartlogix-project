package cl.smartlogix.bff.controller;

import cl.smartlogix.bff.client.GatewayClient;
import cl.smartlogix.bff.config.TestSecurityConfig;
import cl.smartlogix.bff.dto.response.ProductoResponseDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@WebFluxTest(ProductoLogisticaController.class)
@Import(TestSecurityConfig.class)
class ProductoLogisticaControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private GatewayClient gatewayClient;

    @Test
    void listar_returnsProducts() {
        when(gatewayClient.getProductos(eq("mock-jwt"), any()))
                .thenReturn(Mono.just(List.of(new ProductoResponseDTO())));

        webTestClient.get().uri("/bff/logistica/productos")
                .header("Authorization", "Bearer mock-jwt")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void crear_returnsCreated() {
        when(gatewayClient.crearProducto(any(), eq("mock-jwt"), any()))
                .thenReturn(Mono.just(new ProductoResponseDTO()));

        webTestClient.post().uri("/bff/logistica/productos")
                .header("Authorization", "Bearer mock-jwt")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{}")
                .exchange()
                .expectStatus().isCreated();
    }

    @Test
    void obtener_returnsProduct() {
        when(gatewayClient.obtenerProducto(eq(1L), eq("mock-jwt"), any()))
                .thenReturn(Mono.just(new ProductoResponseDTO()));

        webTestClient.get().uri("/bff/logistica/productos/1")
                .header("Authorization", "Bearer mock-jwt")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void actualizar_returnsProduct() {
        when(gatewayClient.actualizarProducto(eq(1L), any(), eq("mock-jwt"), any()))
                .thenReturn(Mono.just(new ProductoResponseDTO()));

        webTestClient.put().uri("/bff/logistica/productos/1")
                .header("Authorization", "Bearer mock-jwt")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{}")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void eliminar_returnsNoContent() {
        when(gatewayClient.eliminarProducto(eq(1L), eq("mock-jwt"), any()))
                .thenReturn(Mono.empty());

        webTestClient.delete().uri("/bff/logistica/productos/1")
                .header("Authorization", "Bearer mock-jwt")
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void listar_withInvalidAuth_returnsUnauthorized() {
        webTestClient.get().uri("/bff/logistica/productos")
                .header("Authorization", "InvalidToken")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void listar_withoutAuth_returnsUnauthorized() {
        webTestClient.get().uri("/bff/logistica/productos")
                .exchange()
                .expectStatus().isUnauthorized();
    }
}
