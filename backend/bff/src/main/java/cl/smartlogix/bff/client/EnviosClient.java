package cl.smartlogix.bff.client;

import cl.smartlogix.bff.dto.response.EnvioResponseDTO;
import cl.smartlogix.bff.exception.ResourceNotFoundException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class EnviosClient {
        @Qualifier("enviosWebClient")
        private final WebClient enviosWebClient;

        // Listar todos los envíos
        @CircuitBreaker(name = "envios", fallbackMethod = "fallbackListarEnvios")
        public Mono<List<EnvioResponseDTO>> listarEnvios() {
                return enviosWebClient
                                .get()
                                .uri("/api/envios")
                                .retrieve()
                                .onStatus(HttpStatusCode::isError, response -> response.bodyToMono(String.class)
                                                .flatMap(body -> Mono.error(new RuntimeException(
                                                                "Error al listar envíos: " + body))))
                                .bodyToFlux(EnvioResponseDTO.class)
                                .collectList();
        }

        // Obtener un envío por ID
        @CircuitBreaker(name = "envios", fallbackMethod = "fallbackObtenerEnvio")
        public Mono<EnvioResponseDTO> obtenerEnvio(Long id) {
                return enviosWebClient
                                .get()
                                .uri("/api/envios/{id}", id)
                                .retrieve()
                                .onStatus(HttpStatusCode::isError, response -> {
                                        if (response.statusCode() == HttpStatus.NOT_FOUND) {
                                                return response.bodyToMono(String.class)
                                                                .flatMap(body -> Mono
                                                                                .error(new ResourceNotFoundException(
                                                                                                "Envío no encontrado: "
                                                                                                                + body)));
                                        }
                                        return response.bodyToMono(String.class)
                                                        .flatMap(body -> Mono
                                                                        .error(new RuntimeException("Error "
                                                                                        + response.statusCode() + ": "
                                                                                        + body)));
                                })
                                .bodyToMono(EnvioResponseDTO.class);
        }

        // Obtener un envío por ID de pedido
        @CircuitBreaker(name = "envios", fallbackMethod = "fallbackObtenerEnvioPorPedidoId")
        public Mono<EnvioResponseDTO> obtenerEnvioPorPedidoId(Long pedidoId) {
                return enviosWebClient
                                .get()
                                .uri("/api/envios/pedido/{pedidoId}", pedidoId)
                                .retrieve()
                                .onStatus(HttpStatusCode::isError, response -> {
                                        if (response.statusCode() == HttpStatus.NOT_FOUND) {
                                                return response.bodyToMono(String.class)
                                                                .flatMap(body -> Mono
                                                                                .error(new ResourceNotFoundException(
                                                                                                "Envío no encontrado para el pedido: "
                                                                                                                + pedidoId)));
                                        }
                                        return response.bodyToMono(String.class)
                                                        .flatMap(body -> Mono.error(new RuntimeException("Error "
                                                                        + response.statusCode() + ": " + body)));
                                })
                                .bodyToMono(EnvioResponseDTO.class);
        }

        // Fallbacks
        private Mono<List<EnvioResponseDTO>> fallbackListarEnvios(Throwable t) {
                log.error("Circuit breaker abierto para listarEnvios. Motivo: {}", t.getMessage());
                return Mono.error(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                                "El servicio de envíos no está disponible para listar. Intente más tarde."));
        }

        private Mono<EnvioResponseDTO> fallbackObtenerEnvio(Long id, Throwable t) {
                log.error("Circuit breaker abierto para obtenerEnvio {}. Motivo: {}", id, t.getMessage());
                return Mono.error(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                                "El servicio de envíos no está disponible. Intente más tarde."));
        }

        private Mono<EnvioResponseDTO> fallbackObtenerEnvioPorPedidoId(Long pedidoId, Throwable t) {
                log.error("Circuit breaker abierto para obtenerEnvioPorPedidoId {}. Motivo: {}", pedidoId,
                                t.getMessage());
                return Mono.error(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                                "El servicio de envíos no está disponible. Intente más tarde."));
        }
}