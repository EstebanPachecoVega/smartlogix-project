package cl.smartlogix.bff.client;

import cl.smartlogix.bff.dto.request.CrearPedidoRequestDTO;
import cl.smartlogix.bff.dto.response.PedidoResponseDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class PedidosClientService {
    private final WebClient pedidosWebClient;

    @CircuitBreaker(name = "pedidos", fallbackMethod = "fallbackCrearPedido")
    public Mono<PedidoResponseDTO> crearPedido(CrearPedidoRequestDTO request) {
        return pedidosWebClient
                .post()
                .uri("/api/pedidos")
                .bodyValue(request)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new RuntimeException("Error: " + body))))
                .bodyToMono(PedidoResponseDTO.class);
    }

    @CircuitBreaker(name = "pedidos", fallbackMethod = "fallbackObtenerPedido")
    public Mono<PedidoResponseDTO> obtenerPedido(Long id) {
        return pedidosWebClient
                .get()
                .uri("/api/pedidos/{id}", id)
                .retrieve()
                .bodyToMono(PedidoResponseDTO.class);
    }

    // Fallbacks (deben devolver Mono)
    private Mono<PedidoResponseDTO> fallbackCrearPedido(CrearPedidoRequestDTO request, Throwable t) {
        log.error("Circuit breaker abierto para crearPedido. Motivo: {}", t.getMessage());
        return Mono.error(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                "El servicio de pedidos no está disponible. Intente más tarde."));
    }

    private Mono<PedidoResponseDTO> fallbackObtenerPedido(Long id, Throwable t) {
        log.error("Circuit breaker abierto para obtenerPedido {}. Motivo: {}", id, t.getMessage());
        return Mono.error(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                "El servicio de pedidos no está disponible. Intente más tarde."));
    }
}