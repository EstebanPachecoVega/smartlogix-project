package cl.smartlogix.bff.client;

import cl.smartlogix.bff.dto.request.*;
import cl.smartlogix.bff.dto.response.*;
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

    // ==================== PRODUCTOS (cliente) ====================
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

    // ==================== PRODUCTOS (gestión) ====================
    @CircuitBreaker(name = "gateway", fallbackMethod = "fallbackCrearProducto")
    public Mono<ProductoResponseDTO> crearProducto(ProductoRequestDTO request, String jwtToken, String correlationId) {
        return gatewayWebClient
                .post()
                .uri("/api/productos")
                .header("Authorization", "Bearer " + jwtToken)
                .header("X-Correlation-Id", correlationId)
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleError)
                .bodyToMono(ProductoResponseDTO.class);
    }

    @CircuitBreaker(name = "gateway", fallbackMethod = "fallbackActualizarProducto")
    public Mono<ProductoResponseDTO> actualizarProducto(Long id, ProductoRequestDTO request, String jwtToken,
            String correlationId) {
        return gatewayWebClient
                .put()
                .uri("/api/productos/" + id)
                .header("Authorization", "Bearer " + jwtToken)
                .header("X-Correlation-Id", correlationId)
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleError)
                .bodyToMono(ProductoResponseDTO.class);
    }

    @CircuitBreaker(name = "gateway", fallbackMethod = "fallbackEliminarProducto")
    public Mono<Void> eliminarProducto(Long id, String jwtToken, String correlationId) {
        return gatewayWebClient
                .delete()
                .uri("/api/productos/" + id)
                .header("Authorization", "Bearer " + jwtToken)
                .header("X-Correlation-Id", correlationId)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleError)
                .bodyToMono(Void.class);
    }

    // ==================== CATEGORÍAS (Gestión CRUD) ====================
    @CircuitBreaker(name = "gateway", fallbackMethod = "fallbackListarCategorias")
    public Mono<List<CategoriaResponseDTO>> listarCategorias(String jwtToken, String correlationId) {
        return gatewayWebClient
                .get()
                .uri("/api/categorias")
                .header("Authorization", "Bearer " + jwtToken)
                .header("X-Correlation-Id", correlationId)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleError)
                .bodyToFlux(CategoriaResponseDTO.class)
                .collectList();
    }

    @CircuitBreaker(name = "gateway", fallbackMethod = "fallbackObtenerCategoria")
    public Mono<CategoriaResponseDTO> obtenerCategoria(Long id, String jwtToken, String correlationId) {
        return gatewayWebClient
                .get()
                .uri("/api/categorias/" + id)
                .header("Authorization", "Bearer " + jwtToken)
                .header("X-Correlation-Id", correlationId)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleError)
                .bodyToMono(CategoriaResponseDTO.class);
    }

    @CircuitBreaker(name = "gateway", fallbackMethod = "fallbackCrearCategoria")
    public Mono<CategoriaResponseDTO> crearCategoria(CategoriaRequestDTO request, String jwtToken,
            String correlationId) {
        return gatewayWebClient
                .post()
                .uri("/api/categorias")
                .header("Authorization", "Bearer " + jwtToken)
                .header("X-Correlation-Id", correlationId)
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleError)
                .bodyToMono(CategoriaResponseDTO.class);
    }

    @CircuitBreaker(name = "gateway", fallbackMethod = "fallbackActualizarCategoria")
    public Mono<CategoriaResponseDTO> actualizarCategoria(Long id, CategoriaRequestDTO request, String jwtToken,
            String correlationId) {
        return gatewayWebClient
                .put()
                .uri("/api/categorias/" + id)
                .header("Authorization", "Bearer " + jwtToken)
                .header("X-Correlation-Id", correlationId)
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleError)
                .bodyToMono(CategoriaResponseDTO.class);
    }

    @CircuitBreaker(name = "gateway", fallbackMethod = "fallbackEliminarCategoria")
    public Mono<Void> eliminarCategoria(Long id, String jwtToken, String correlationId) {
        return gatewayWebClient
                .delete()
                .uri("/api/categorias/" + id)
                .header("Authorization", "Bearer " + jwtToken)
                .header("X-Correlation-Id", correlationId)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleError)
                .bodyToMono(Void.class);
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

    @CircuitBreaker(name = "gateway", fallbackMethod = "fallbackObtenerPedido")
    public Mono<PedidoResponseDTO> obtenerPedido(Long id, String jwtToken, String correlationId) {
        return gatewayWebClient
                .get()
                .uri("/api/pedidos/" + id)
                .header("Authorization", "Bearer " + jwtToken)
                .header("X-Correlation-Id", correlationId)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleError)
                .bodyToMono(PedidoResponseDTO.class);
    }

    // ==================== ENVÍOS ====================
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

    @CircuitBreaker(name = "gateway", fallbackMethod = "fallbackObtenerEnvio")
    public Mono<EnvioResponseDTO> obtenerEnvio(Long id, String jwtToken, String correlationId) {
        return gatewayWebClient
                .get()
                .uri("/api/envios/" + id)
                .header("Authorization", "Bearer " + jwtToken)
                .header("X-Correlation-Id", correlationId)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleError)
                .bodyToMono(EnvioResponseDTO.class);
    }

    @CircuitBreaker(name = "gateway", fallbackMethod = "fallbackObtenerEnvioPorPedidoId")
    public Mono<EnvioResponseDTO> obtenerEnvioPorPedidoId(Long pedidoId, String jwtToken, String correlationId) {
        return gatewayWebClient
                .get()
                .uri("/api/envios/pedido/" + pedidoId)
                .header("Authorization", "Bearer " + jwtToken)
                .header("X-Correlation-Id", correlationId)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleError)
                .bodyToMono(EnvioResponseDTO.class);
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
    private Mono<List<ProductoResponseDTO>> fallbackGetProductos(String jwt, String cid, Throwable t) {
        log.error("CB abierto getProductos: {}", t.getMessage());
        return Mono.error(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Productos no disponible"));
    }

    private Mono<ProductoResponseDTO> fallbackCrearProducto(ProductoRequestDTO req, String jwt, String cid,
            Throwable t) {
        log.error("CB abierto crearProducto: {}", t.getMessage());
        return Mono.error(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Productos no disponible"));
    }

    private Mono<ProductoResponseDTO> fallbackActualizarProducto(Long id, ProductoRequestDTO req, String jwt,
            String cid, Throwable t) {
        log.error("CB abierto actualizarProducto: {}", t.getMessage());
        return Mono.error(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Productos no disponible"));
    }

    private Mono<Void> fallbackEliminarProducto(Long id, String jwt, String cid, Throwable t) {
        log.error("CB abierto eliminarProducto: {}", t.getMessage());
        return Mono.error(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Productos no disponible"));
    }

    private Mono<List<CategoriaResponseDTO>> fallbackListarCategorias(String jwt, String cid, Throwable t) {
        log.error("CB abierto listarCategorias: {}", t.getMessage());
        return Mono.error(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Categorías no disponible"));
    }

    private Mono<CategoriaResponseDTO> fallbackCrearCategoria(CategoriaRequestDTO req, String jwt, String cid,
            Throwable t) {
        log.error("CB abierto crearCategoria: {}", t.getMessage());
        return Mono.error(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Categorías no disponible"));
    }

    private Mono<CategoriaResponseDTO> fallbackActualizarCategoria(Long id, CategoriaRequestDTO req, String jwt,
            String cid, Throwable t) {
        log.error("CB abierto actualizarCategoria: {}", t.getMessage());
        return Mono.error(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Categorías no disponible"));
    }

    private Mono<Void> fallbackEliminarCategoria(Long id, String jwt, String cid, Throwable t) {
        log.error("CB abierto eliminarCategoria: {}", t.getMessage());
        return Mono.error(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Categorías no disponible"));
    }

    private Mono<PedidoResponseDTO> fallbackCrearPedido(CrearPedidoRequestDTO req, String jwt, String cid,
            Throwable t) {
        log.error("CB abierto crearPedido: {}", t.getMessage());
        return Mono.error(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Pedidos no disponible"));
    }

    private Mono<List<PedidoResponseDTO>> fallbackListarPedidos(String jwt, String cid, Throwable t) {
        log.error("CB abierto listarPedidos: {}", t.getMessage());
        return Mono.error(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Pedidos no disponible"));
    }

    private Mono<PedidoResponseDTO> fallbackObtenerPedido(Long id, String jwt, String cid, Throwable t) {
        log.error("CB abierto obtenerPedido: {}", t.getMessage());
        return Mono.error(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Pedidos no disponible"));
    }

    private Mono<List<EnvioResponseDTO>> fallbackListarEnvios(String jwt, String cid, Throwable t) {
        log.error("CB abierto listarEnvios: {}", t.getMessage());
        return Mono.error(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Envíos no disponible"));
    }

    private Mono<EnvioResponseDTO> fallbackObtenerEnvio(Long id, String jwt, String cid, Throwable t) {
        log.error("CB abierto obtenerEnvio: {}", t.getMessage());
        return Mono.error(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Envíos no disponible"));
    }

    private Mono<EnvioResponseDTO> fallbackObtenerEnvioPorPedidoId(Long pedidoId, String jwtToken, String correlationId,
            Throwable t) {
        log.error("Circuit breaker abierto para obtenerEnvioPorPedidoId {}: {}", pedidoId, t.getMessage());
        return Mono
                .error(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Servicio de envíos no disponible"));
    }    

    private Mono<EnvioResponseDTO> fallbackActualizarEstadoEnvio(Long id, String estado, String jwt, String cid,
            Throwable t) {
        log.error("CB abierto actualizarEstadoEnvio: {}", t.getMessage());
        return Mono.error(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Envíos no disponible"));
    }

    private Mono<EnvioResponseDTO> fallbackObtenerEnvioPorTracking(String tracking, String jwt, String cid,
            Throwable t) {
        log.error("CB abierto obtenerEnvioPorTracking: {}", t.getMessage());
        return Mono.error(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Envíos no disponible"));
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