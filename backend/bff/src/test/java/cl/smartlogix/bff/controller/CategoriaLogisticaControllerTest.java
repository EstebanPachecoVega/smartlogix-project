package cl.smartlogix.bff.controller;

import cl.smartlogix.bff.client.GatewayClient;
import cl.smartlogix.bff.config.TestSecurityConfig;
import cl.smartlogix.bff.dto.request.CategoriaRequestDTO;
import cl.smartlogix.bff.dto.request.ReordenarCategoriaDTO;
import cl.smartlogix.bff.dto.response.CategoriaResponseDTO;
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

@WebFluxTest(CategoriaLogisticaController.class)
@Import(TestSecurityConfig.class)
class CategoriaLogisticaControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private GatewayClient gatewayClient;

    @Test
    void listar_returnsCategories() {
        when(gatewayClient.listarCategorias(eq("mock-jwt"), any()))
                .thenReturn(Mono.just(List.of(new CategoriaResponseDTO())));

        webTestClient.get().uri("/bff/logistica/categorias")
                .header("Authorization", "Bearer mock-jwt")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void obtener_returnsCategoria() {
        when(gatewayClient.obtenerCategoria(eq(1L), eq("mock-jwt"), any()))
                .thenReturn(Mono.just(new CategoriaResponseDTO()));

        webTestClient.get().uri("/bff/logistica/categorias/1")
                .header("Authorization", "Bearer mock-jwt")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void crear_returnsCreated() {
        when(gatewayClient.crearCategoria(any(), eq("mock-jwt"), any()))
                .thenReturn(Mono.just(new CategoriaResponseDTO()));

        webTestClient.post().uri("/bff/logistica/categorias")
                .header("Authorization", "Bearer mock-jwt")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{}")
                .exchange()
                .expectStatus().isCreated();
    }

    @Test
    void actualizar_returnsCategoria() {
        when(gatewayClient.actualizarCategoria(eq(1L), any(), eq("mock-jwt"), any()))
                .thenReturn(Mono.just(new CategoriaResponseDTO()));

        webTestClient.put().uri("/bff/logistica/categorias/1")
                .header("Authorization", "Bearer mock-jwt")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{}")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void eliminar_returnsNoContent() {
        when(gatewayClient.eliminarCategoria(eq(1L), eq("mock-jwt"), any()))
                .thenReturn(Mono.empty());

        webTestClient.delete().uri("/bff/logistica/categorias/1")
                .header("Authorization", "Bearer mock-jwt")
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void reordenar_returnsNoContent() {
        when(gatewayClient.reordenarCategorias(anyList(), eq("mock-jwt"), any()))
                .thenReturn(Mono.empty());

        webTestClient.patch().uri("/bff/logistica/categorias/reordenar")
                .header("Authorization", "Bearer mock-jwt")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(List.of(new ReordenarCategoriaDTO(1L, 1)))
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void listar_withInvalidAuth_returnsUnauthorized() {
        webTestClient.get().uri("/bff/logistica/categorias")
                .header("Authorization", "InvalidToken")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void crear_withInvalidAuth_returnsUnauthorized() {
        webTestClient.post().uri("/bff/logistica/categorias")
                .header("Authorization", "InvalidToken")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{}")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void listar_withoutAuth_returnsUnauthorized() {
        webTestClient.get().uri("/bff/logistica/categorias")
                .exchange()
                .expectStatus().isUnauthorized();
    }
}
