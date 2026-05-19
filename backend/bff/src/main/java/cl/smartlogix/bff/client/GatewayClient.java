package cl.smartlogix.bff.client;

import cl.smartlogix.bff.dto.request.CrearPedidoRequestDTO;
import cl.smartlogix.bff.dto.response.PedidoResponseDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class GatewayClient {

    private final WebClient gatewayWebClient;

    @CircuitBreaker(name = "gateway", fallbackMethod = "fallbackCrearPedido")
    public Mono<PedidoResponseDTO> crearPedido(CrearPedidoRequestDTO request, String jwtToken, String correlationId) {
        return gatewayWebClient
                .post()
                .uri("/api/pedidos")
                .header("Authorization", "Bearer " + jwtToken)
                .header("X-Correlation-Id", correlationId)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(PedidoResponseDTO.class);
    }

    @CircuitBreaker(name = "gateway", fallbackMethod = "fallbackListarPedidos")
    public Mono<List<PedidoResponseDTO>> listarPedidos(String jwtToken, String correlationId) {
        return gatewayWebClient
                .get()
                .uri("/api/pedidos")
                .header("Authorization", "Bearer " + jwtToken)
                .header("X-Correlation-Id", correlationId)
                .retrieve()
                .bodyToFlux(PedidoResponseDTO.class)
                .collectList();
    }

    @CircuitBreaker(name = "gateway", fallbackMethod = "fallbackObtenerPedido")
    public Mono<PedidoResponseDTO> obtenerPedido(Long id, String jwtToken, String correlationId) {
        return gatewayWebClient
                .get()
                .uri("/api/pedidos/{id}", id)
                .header("Authorization", "Bearer " + jwtToken)
                .header("X-Correlation-Id", correlationId)
                .retrieve()
                .bodyToMono(PedidoResponseDTO.class);
    }

    // Fallbacks
    private Mono<PedidoResponseDTO> fallbackCrearPedido(CrearPedidoRequestDTO request, String jwtToken, String correlationId, Throwable t) {
        log.error("Circuit breaker abierto para crearPedido: {}", t.getMessage());
        return Mono.error(new RuntimeException("Servicio de pedidos no disponible temporalmente"));
    }

    private Mono<List<PedidoResponseDTO>> fallbackListarPedidos(String jwtToken, String correlationId, Throwable t) {
        log.error("Circuit breaker abierto para listarPedidos: {}", t.getMessage());
        return Mono.error(new RuntimeException("Servicio de pedidos no disponible temporalmente"));
    }

    private Mono<PedidoResponseDTO> fallbackObtenerPedido(Long id, String jwtToken, String correlationId, Throwable t) {
        log.error("Circuit breaker abierto para obtenerPedido {}: {}", id, t.getMessage());
        return Mono.error(new RuntimeException("Servicio de pedidos no disponible temporalmente"));
    }
}