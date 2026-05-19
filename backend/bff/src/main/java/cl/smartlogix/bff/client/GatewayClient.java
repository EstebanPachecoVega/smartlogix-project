package cl.smartlogix.bff.client;

import cl.smartlogix.bff.dto.request.CrearPedidoRequestDTO;
import cl.smartlogix.bff.dto.response.PedidoResponseDTO;
import cl.smartlogix.bff.dto.response.ProductoResponseDTO;
import cl.smartlogix.bff.dto.response.EnvioResponseDTO;
import cl.smartlogix.bff.exception.DomainException;
import cl.smartlogix.bff.exception.ResourceNotFoundException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class GatewayClient {

    private final WebClient gatewayWebClient;

    // ==================== PRODUCTOS ====================
    @CircuitBreaker(name = "gateway", fallbackMethod = "fallbackGetProductos")
    public Mono<List<ProductoResponseDTO>> getProductos(String jwtToken, String correlationId) {
        return gatewayWebClient
                .get()
                .uri("/api/productos")
                .header("Authorization", "Bearer " + jwtToken)
                .header("X-Correlation-Id", correlationId)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleError)
                .bodyToFlux(ProductoResponseDTO.class)
                .collectList();
    }

    private Mono<List<ProductoResponseDTO>> fallbackGetProductos(String jwtToken, String correlationId, Throwable t) {
        log.error("Circuit breaker abierto para getProductos: {}", t.getMessage());
        return Mono.error(
                new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Servicio de productos no disponible"));
    }

    // ==================== PEDIDOS ====================
    @CircuitBreaker(name = "gateway", fallbackMethod = "fallbackCrearPedido")
    public Mono<PedidoResponseDTO> crearPedido(CrearPedidoRequestDTO request, String jwtToken, String correlationId) {
        return gatewayWebClient
                .post()
                .uri("/api/pedidos")
                .header("Authorization", "Bearer " + jwtToken)
                .header("X-Correlation-Id", correlationId)
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleError)
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
                .onStatus(HttpStatusCode::isError, this::handleError)
                .bodyToFlux(PedidoResponseDTO.class)
                .collectList();
    }

    private Mono<PedidoResponseDTO> fallbackCrearPedido(CrearPedidoRequestDTO request, String jwtToken,
            String correlationId, Throwable t) {
        log.error("Circuit breaker abierto para crearPedido: {}", t.getMessage());
        return Mono.error(
                new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Servicio de pedidos no disponible"));
    }

    private Mono<List<PedidoResponseDTO>> fallbackListarPedidos(String jwtToken, String correlationId, Throwable t) {
        log.error("Circuit breaker abierto para listarPedidos: {}", t.getMessage());
        return Mono.error(
                new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Servicio de pedidos no disponible"));
    }

    // ==================== ENVÍOS ====================
    @CircuitBreaker(name = "gateway", fallbackMethod = "fallbackObtenerEnvioPorId")
    public Mono<EnvioResponseDTO> obtenerEnvioPorId(Long envioId, String jwtToken, String correlationId) {
        return gatewayWebClient
                .get()
                .uri("/api/envios/" + envioId)
                .header("Authorization", "Bearer " + jwtToken)
                .header("X-Correlation-Id", correlationId)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleError)
                .bodyToMono(EnvioResponseDTO.class);
    }

    @CircuitBreaker(name = "gateway", fallbackMethod = "fallbackListarEnvios")
    public Mono<List<EnvioResponseDTO>> listarEnvios(String jwtToken, String correlationId) {
        return gatewayWebClient
                .get()
                .uri("/api/envios")
                .header("Authorization", "Bearer " + jwtToken)
                .header("X-Correlation-Id", correlationId)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleError)
                .bodyToFlux(EnvioResponseDTO.class)
                .collectList();
    }

    @CircuitBreaker(name = "gateway", fallbackMethod = "fallbackActualizarEstadoEnvio")
    public Mono<EnvioResponseDTO> actualizarEstadoEnvio(Long envioId, String nuevoEstado, String jwtToken,
            String correlationId) {
        return gatewayWebClient
                .patch()
                .uri("/api/envios/" + envioId + "/estado?nuevoEstado=" + nuevoEstado)
                .header("Authorization", "Bearer " + jwtToken)
                .header("X-Correlation-Id", correlationId)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleError)
                .bodyToMono(EnvioResponseDTO.class);
    }

    @CircuitBreaker(name = "gateway", fallbackMethod = "fallbackObtenerEnvioPorTracking")
    public Mono<EnvioResponseDTO> obtenerEnvioPorTracking(String tracking, String jwtToken, String correlationId) {
        return gatewayWebClient
                .get()
                .uri("/api/envios/tracking/" + tracking)
                .header("Authorization", "Bearer " + jwtToken)
                .header("X-Correlation-Id", correlationId)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleError)
                .bodyToMono(EnvioResponseDTO.class);
    }

    // ==================== FALLBACKS ====================
    private Mono<EnvioResponseDTO> fallbackObtenerEnvioPorId(Long envioId, String jwtToken, String correlationId,
            Throwable t) {
        log.error("Circuit breaker abierto para obtenerEnvioPorId {}: {}", envioId, t.getMessage());
        return Mono
                .error(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Servicio de envíos no disponible"));
    }

    private Mono<List<EnvioResponseDTO>> fallbackListarEnvios(String jwtToken, String correlationId, Throwable t) {
        log.error("Circuit breaker abierto para listarEnvios: {}", t.getMessage());
        return Mono
                .error(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Servicio de envíos no disponible"));
    }

    private Mono<EnvioResponseDTO> fallbackActualizarEstadoEnvio(Long envioId, String nuevoEstado, String jwtToken,
            String correlationId, Throwable t) {
        log.error("Circuit breaker abierto para actualizarEstadoEnvio: {}", t.getMessage());
        return Mono
                .error(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Servicio de envíos no disponible"));
    }

    private Mono<EnvioResponseDTO> fallbackObtenerEnvioPorTracking(String tracking, String jwtToken,
            String correlationId, Throwable t) {
        log.error("Circuit breaker abierto para obtenerEnvioPorTracking: {}", t.getMessage());
        return Mono
                .error(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Servicio de envíos no disponible"));
    }

    // Manejo común de errores
    private Mono<? extends Throwable> handleError(ClientResponse response) {
        HttpStatusCode status = response.statusCode();
        return response.bodyToMono(String.class)
                .flatMap(body -> {
                    if (status == HttpStatus.NOT_FOUND) {
                        return Mono.error(new ResourceNotFoundException(body));
                    } else if (status == HttpStatus.UNPROCESSABLE_ENTITY) {
                        return Mono.error(new DomainException(body));
                    } else {
                        return Mono.error(new RuntimeException("Error " + status + ": " + body));
                    }
                });
    }
}