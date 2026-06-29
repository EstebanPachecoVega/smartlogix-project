package cl.smartlogix.gateway.exception;

import cl.smartlogix.gateway.config.TestSecurityConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@WebFluxTest
@Import({TestSecurityConfig.class, GlobalExceptionHandler.class, GlobalExceptionHandlerTest.TestController.class})
class GlobalExceptionHandlerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private GlobalExceptionHandler handler;

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

    @Test
    void serializationError_completesResponse() throws Exception {
        ObjectMapper original = handler.mapper;
        try {
            ObjectMapper brokenMapper = mock(ObjectMapper.class);
            when(brokenMapper.writeValueAsBytes(any())).thenThrow(new JsonProcessingException("broken") {});
            handler.mapper = brokenMapper;

            webTestClient.get().uri("/test/not-found")
                    .exchange()
                    .expectStatus().isNotFound();
        } finally {
            handler.mapper = original;
        }
    }

    @RestController
    static class TestController {
        @GetMapping("/test/not-found")
        Mono<Void> notFound() {
            throw new ResourceNotFoundException("Not found");
        }

        @GetMapping("/test/domain-error")
        Mono<Void> domainError() {
            throw new DomainException("Domain error");
        }

        @GetMapping("/test/generic-error")
        Mono<Void> genericError() {
            throw new RuntimeException("Generic error");
        }
    }
}
