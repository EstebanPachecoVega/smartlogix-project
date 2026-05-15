package cl.smartlogix.bff.client;

import cl.smartlogix.bff.dto.response.EnvioResponseDTO;
import cl.smartlogix.bff.exception.ResourceNotFoundException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class EnviosClientService {
    private final WebClient enviosWebClient;

    @CircuitBreaker(name = "envios", fallbackMethod = "fallbackObtenerEnvio")
    public Mono<EnvioResponseDTO> obtenerEnvio(Long id) {
        return enviosWebClient
                .get()
                .uri("/api/envios/{id}", id)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> {
                    if (response.statusCode() == HttpStatus.NOT_FOUND) {
                        return response.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new ResourceNotFoundException("Envío no encontrado: " + body)));
                    }
                    return response.bodyToMono(String.class)
                            .flatMap(body -> Mono.error(new RuntimeException("Error " + response.statusCode() + ": " + body)));
                })
                .bodyToMono(EnvioResponseDTO.class);
    }

    private Mono<EnvioResponseDTO> fallbackObtenerEnvio(Long id, Throwable t) {
        log.error("Circuit breaker abierto para obtenerEnvio {}. Motivo: {}", id, t.getMessage());
        return Mono.error(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                "El servicio de envíos no está disponible. Intente más tarde."));
    }
}