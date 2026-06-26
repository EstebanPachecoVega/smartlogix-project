package cl.smartlogix.bff.controller;

import cl.smartlogix.bff.client.GatewayClient;
import cl.smartlogix.bff.config.TestSecurityConfig;
import cl.smartlogix.bff.dto.response.PagedResponse;
import cl.smartlogix.bff.dto.response.ProductoResponseDTO;
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

@WebFluxTest(ProductoBffController.class)
@Import(TestSecurityConfig.class)
class ProductoBffControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private GatewayClient gatewayClient;

    @Test
    void listar_returnsProducts() {
        PagedResponse<ProductoResponseDTO> paged = new PagedResponse<>();
        paged.setContent(List.of(new ProductoResponseDTO()));
        when(gatewayClient.getProductos(eq("mock-jwt"), any(), anyInt(), anyInt()))
                .thenReturn(Mono.just(paged));

        webTestClient.get().uri("/bff/productos")
                .header("Authorization", "Bearer mock-jwt")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void listar_withoutAuth_returnsUnauthorized() {
        webTestClient.get().uri("/bff/productos")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void listar_withInvalidAuth_returnsUnauthorized() {
        webTestClient.get().uri("/bff/productos")
                .header("Authorization", "InvalidToken")
                .exchange()
                .expectStatus().isUnauthorized();
    }
}
