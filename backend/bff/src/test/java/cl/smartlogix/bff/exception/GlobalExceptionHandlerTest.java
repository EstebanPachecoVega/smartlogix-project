package cl.smartlogix.bff.exception;

import cl.smartlogix.bff.client.GatewayClient;
import cl.smartlogix.bff.config.TestSecurityConfig;
import cl.smartlogix.bff.dto.response.ProductoResponseDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.when;

@WebFluxTest
@Import(TestSecurityConfig.class)
@ContextConfiguration(classes = {
    GlobalExceptionHandler.class,
    GlobalExceptionHandlerTest.TestController.class
})
class GlobalExceptionHandlerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private GatewayClient gatewayClient;

    @Test
    void notFound_returns404() {
        webTestClient.get().uri("/test/not-found")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.title").isEqualTo("Recurso no encontrado");
    }

    @Test
    void domainException_returns422() {
        webTestClient.get().uri("/test/domain-error")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.title").isEqualTo("Regla de negocio violada");
    }

    @Test
    void genericError_returns500() {
        webTestClient.get().uri("/test/generic-error")
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody()
                .jsonPath("$.title").isEqualTo("Error interno");
    }

    @RestController
    static class TestController {
        @GetMapping("/test/not-found")
        Mono<Void> notFound() {
            throw new ResourceNotFoundException("Test not found");
        }

        @GetMapping("/test/domain-error")
        Mono<Void> domainError() {
            throw new DomainException("Test domain");
        }

        @GetMapping("/test/generic-error")
        Mono<Void> genericError() {
            throw new RuntimeException("Test error");
        }
    }
}
