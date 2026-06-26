package cl.smartlogix.bff.client;

import cl.smartlogix.bff.dto.request.*;
import cl.smartlogix.bff.dto.response.*;
import cl.smartlogix.bff.exception.DomainException;
import cl.smartlogix.bff.exception.ResourceNotFoundException;
import reactor.core.publisher.Flux;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
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
    private final cl.smartlogix.bff.config.ReactiveCacheManager cacheManager;

    // ==================== PRODUCTOS (cliente) ====================
    @Retry(name = "gateway")
    @CircuitBreaker(name = "gateway", fallbackMethod = "fallbackGetProductos")
    public Mono<PagedResponse<ProductoResponseDTO>> getProductos(
            String jwtToken, String correlationId, int page, int size) {
        String cacheKey = "productos:page:" + page + ":size:" + size;
        return cacheManager.getOrFetch(cacheKey, java.time.Duration.ofSeconds(30), () ->
            gatewayWebClient
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/productos")
                            .queryParam("page", page)
                            .queryParam("size", size)
                            .build())
                    .header("Authorization", "Bearer " + jwtToken)
                    .header("X-Correlation-Id", correlationId)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, this::handleError)
                    .bodyToMono(new ParameterizedTypeReference<PagedResponse<ProductoResponseDTO>>() {})
        );
    }

    // ==================== PRODUCTOS (gestión) ====================
    @Retry(name = "gateway")
    @CircuitBreaker(name = "gateway", fallbackMethod = "fallbackCrearProducto")
    public Mono<ProductoResponseDTO> crearProducto(ProductoRequestDTO request, String jwtToken, String correlationId) {
        cacheManager.evictByPrefix("productos:");
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

    @Retry(name = "gateway")
    @CircuitBreaker(name = "gateway", fallbackMethod = "fallbackObtenerProducto")
    public Mono<ProductoResponseDTO> obtenerProducto(Long id, String jwtToken, String correlationId) {
        return gatewayWebClient
                .get()
                .uri("/api/productos/" + id)
                .header("Authorization", "Bearer " + jwtToken)
                .header("X-Correlation-Id", correlationId)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleError)
                .bodyToMono(ProductoResponseDTO.class);
    }

    @Retry(name = "gateway")
    @CircuitBreaker(name = "gateway", fallbackMethod = "fallbackActualizarProducto")
    public Mono<ProductoResponseDTO> actualizarProducto(Long id, ProductoRequestDTO request, String jwtToken,
            String correlationId) {
        cacheManager.evictByPrefix("productos:");
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

    @Retry(name = "gateway")
    @CircuitBreaker(name = "gateway", fallbackMethod = "fallbackEliminarProducto")
    public Mono<Void> eliminarProducto(Long id, String jwtToken, String correlationId) {
        cacheManager.evictByPrefix("productos:");
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
    @Retry(name = "gateway")
    @CircuitBreaker(name = "gateway", fallbackMethod = "fallbackListarCategorias")
    public Mono<PagedResponse<CategoriaResponseDTO>> listarCategorias(
            String jwtToken, String correlationId, int page, int size) {
        String cacheKey = "categorias:page:" + page + ":size:" + size;
        return cacheManager.getOrFetch(cacheKey, java.time.Duration.ofSeconds(120), () ->
            gatewayWebClient
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/categorias")
                            .queryParam("page", page)
                            .queryParam("size", size)
                            .build())
                    .header("Authorization", "Bearer " + jwtToken)
                    .header("X-Correlation-Id", correlationId)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, this::handleError)
                    .bodyToMono(new ParameterizedTypeReference<PagedResponse<CategoriaResponseDTO>>() {})
        );
    }

    @Retry(name = "gateway")
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

    @Retry(name = "gateway")
    @CircuitBreaker(name = "gateway", fallbackMethod = "fallbackCrearCategoria")
    public Mono<CategoriaResponseDTO> crearCategoria(CategoriaRequestDTO request, String jwtToken,
            String correlationId) {
        cacheManager.evictByPrefix("categorias:");
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

    @Retry(name = "gateway")
    @CircuitBreaker(name = "gateway", fallbackMethod = "fallbackActualizarCategoria")
    public Mono<CategoriaResponseDTO> actualizarCategoria(Long id, CategoriaRequestDTO request, String jwtToken,
            String correlationId) {
        cacheManager.evictByPrefix("categorias:");
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

    @Retry(name = "gateway")
    @CircuitBreaker(name = "gateway", fallbackMethod = "fallbackEliminarCategoria")
    public Mono<Void> eliminarCategoria(Long id, String jwtToken, String correlationId) {
        cacheManager.evictByPrefix("categorias:");
        return gatewayWebClient
                .delete()
                .uri("/api/categorias/" + id)
                .header("Authorization", "Bearer " + jwtToken)
                .header("X-Correlation-Id", correlationId)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleError)
                .bodyToMono(Void.class);
    }

    @Retry(name = "gateway")
    @CircuitBreaker(name = "gateway", fallbackMethod = "fallbackReordenarCategorias")
    public Mono<Void> reordenarCategorias(List<ReordenarCategoriaDTO> ordenes, String jwtToken, String correlationId) {
        return gatewayWebClient
                .patch()
                .uri("/api/categorias/reordenar")
                .header("Authorization", "Bearer " + jwtToken)
                .header("X-Correlation-Id", correlationId)
                .bodyValue(ordenes)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleError)
                .bodyToMono(Void.class);
    }

    // ==================== PEDIDOS ====================
    public Mono<PedidoResponseDTO> crearPedido(CrearPedidoRequestDTO request, String jwtToken, String idempotencyKey,
            String correlationId) {
        var requestBuilder = gatewayWebClient.post()
                .uri("/api/pedidos")
                .header("Authorization", "Bearer " + jwtToken)
                .header("X-Correlation-Id", correlationId);
        if (idempotencyKey != null) {
            requestBuilder.header("Idempotency-Key", idempotencyKey);
        }
        return requestBuilder.bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleError)
                .bodyToMono(PedidoResponseDTO.class);
    }

    @Retry(name = "gateway")
    @CircuitBreaker(name = "gateway", fallbackMethod = "fallbackListarPedidos")
    public Mono<PagedResponse<PedidoResponseDTO>> listarPedidos(
            String jwtToken, String correlationId, int page, int size) {
        return gatewayWebClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/pedidos")
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .build())
                .header("Authorization", "Bearer " + jwtToken)
                .header("X-Correlation-Id", correlationId)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleError)
                .bodyToMono(new ParameterizedTypeReference<PagedResponse<PedidoResponseDTO>>() {});
    }

    @Retry(name = "gateway")
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
    @Retry(name = "gateway")
    @CircuitBreaker(name = "gateway", fallbackMethod = "fallbackListarEnvios")
    public Mono<PagedResponse<EnvioResponseDTO>> listarEnvios(
            String jwtToken, String correlationId, int page, int size) {
        return gatewayWebClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/envios")
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .build())
                .header("Authorization", "Bearer " + jwtToken)
                .header("X-Correlation-Id", correlationId)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleError)
                .bodyToMono(new ParameterizedTypeReference<PagedResponse<EnvioResponseDTO>>() {});
    }

    @Retry(name = "gateway")
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

    @Retry(name = "gateway")
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

    @Retry(name = "gateway")
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

    @Retry(name = "gateway")
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

    // ==================== ESTADÍSTICAS ====================
    @Retry(name = "gateway")
    @CircuitBreaker(name = "gateway", fallbackMethod = "fallbackEstadisticas")
    public Mono<List<VentasPlataformaResponseDTO>> getVentasPlataforma(String jwtToken, String correlationId) {
        return cacheManager.getOrFetch("estadisticas:ventas-plataforma", java.time.Duration.ofSeconds(300), () ->
            gatewayWebClient
                    .get()
                    .uri("/api/pedidos/estadisticas/ventas-plataforma")
                    .header("Authorization", "Bearer " + jwtToken)
                    .header("X-Correlation-Id", correlationId)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, this::handleError)
                    .bodyToFlux(VentasPlataformaResponseDTO.class)
                    .collectList()
        );
    }

    @Retry(name = "gateway")
    @CircuitBreaker(name = "gateway", fallbackMethod = "fallbackEstadisticas")
    public Mono<List<ComparacionAnualResponseDTO>> getComparacionAnual(String jwtToken, String correlationId) {
        return cacheManager.getOrFetch("estadisticas:comparacion-anual", java.time.Duration.ofSeconds(300), () ->
            gatewayWebClient
                    .get()
                    .uri("/api/pedidos/estadisticas/comparacion-anual")
                    .header("Authorization", "Bearer " + jwtToken)
                    .header("X-Correlation-Id", correlationId)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, this::handleError)
                    .bodyToFlux(ComparacionAnualResponseDTO.class)
                    .collectList()
        );
    }

    @Retry(name = "gateway")
    @CircuitBreaker(name = "gateway", fallbackMethod = "fallbackEstadisticas")
    public Mono<List<VentaPorProductoResponseDTO>> getVentasPorProducto(String jwtToken, String correlationId) {
        return cacheManager.getOrFetch("estadisticas:ventas-por-producto", java.time.Duration.ofSeconds(300), () ->
            gatewayWebClient
                    .get()
                    .uri("/api/pedidos/estadisticas/ventas-por-producto")
                    .header("Authorization", "Bearer " + jwtToken)
                    .header("X-Correlation-Id", correlationId)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, this::handleError)
                    .bodyToFlux(VentaPorProductoResponseDTO.class)
                    .collectList()
        );
    }

    @Retry(name = "gateway")
    @CircuitBreaker(name = "gateway", fallbackMethod = "fallbackEstadisticas")
    public Mono<List<VentaPorProductoCantidadDTO>> getCantidadPorProducto(String jwtToken, String correlationId) {
        return cacheManager.getOrFetch("estadisticas:cantidad-por-producto", java.time.Duration.ofSeconds(300), () ->
            gatewayWebClient
                    .get()
                    .uri("/api/pedidos/estadisticas/ventas-por-producto-cantidad")
                    .header("Authorization", "Bearer " + jwtToken)
                    .header("X-Correlation-Id", correlationId)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, this::handleError)
                    .bodyToFlux(VentaPorProductoCantidadDTO.class)
                    .collectList()
        );
    }

    @Retry(name = "gateway")
    @CircuitBreaker(name = "gateway", fallbackMethod = "fallbackEstadisticas")
    public Mono<List<MapaCategoriaResponseDTO>> getMapaCategorias(String jwtToken, String correlationId) {
        return cacheManager.getOrFetch("estadisticas:mapa-categorias", java.time.Duration.ofSeconds(300), () ->
            gatewayWebClient
                    .get()
                    .uri("/api/productos/mapa-categorias")
                    .header("Authorization", "Bearer " + jwtToken)
                    .header("X-Correlation-Id", correlationId)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, this::handleError)
                    .bodyToFlux(MapaCategoriaResponseDTO.class)
                    .collectList()
        );
    }

    // ==================== FALLBACKS ====================
    private Mono<PagedResponse<ProductoResponseDTO>> fallbackGetProductos(
            String jwt, String cid, int page, int size, Throwable t) {
        log.error("CB abierto getProductos: {}", t.getMessage());
        return Mono.error(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Productos no disponible"));
    }

    private Mono<ProductoResponseDTO> fallbackCrearProducto(ProductoRequestDTO req, String jwt, String cid,
            Throwable t) {
        log.error("CB abierto crearProducto: {}", t.getMessage());
        return Mono.error(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Productos no disponible"));
    }

    private Mono<ProductoResponseDTO> fallbackObtenerProducto(Long id, String jwt, String cid, Throwable t) {
        log.error("CB abierto obtenerProducto {}: {}", id, t.getMessage());
        return Mono.error(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Producto no disponible"));
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

    private Mono<PagedResponse<CategoriaResponseDTO>> fallbackListarCategorias(
            String jwt, String cid, int page, int size, Throwable t) {
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

    private Mono<Void> fallbackReordenarCategorias(List<ReordenarCategoriaDTO> ordenes, String jwt, String cid,
            Throwable t) {
        log.error("CB abierto reordenarCategorias: {}", t.getMessage());
        return Mono.error(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Categorías no disponible"));
    }

    private Mono<PedidoResponseDTO> fallbackCrearPedido(CrearPedidoRequestDTO req, String jwt, String cid,
            Throwable t) {
        log.error("CB abierto crearPedido: {}", t.getMessage());
        return Mono.error(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Pedidos no disponible"));
    }

    private Mono<PagedResponse<PedidoResponseDTO>> fallbackListarPedidos(
            String jwt, String cid, int page, int size, Throwable t) {
        log.error("CB abierto listarPedidos: {}", t.getMessage());
        return Mono.error(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Pedidos no disponible"));
    }

    private Mono<PedidoResponseDTO> fallbackObtenerPedido(Long id, String jwt, String cid, Throwable t) {
        log.error("CB abierto obtenerPedido: {}", t.getMessage());
        return Mono.error(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Pedidos no disponible"));
    }

    private Mono<PagedResponse<EnvioResponseDTO>> fallbackListarEnvios(
            String jwt, String cid, int page, int size, Throwable t) {
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

    private <T> Mono<List<T>> fallbackEstadisticas(String jwt, String cid, Throwable t) {
        log.error("CB abierto estadisticas: {}", t.getMessage());
        return Mono.error(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Estadísticas no disponible"));
    }

    // Manejo común de errores
    private Mono<? extends Throwable> handleError(ClientResponse response) {
        HttpStatusCode status = response.statusCode();
        return response.bodyToMono(String.class)
                .defaultIfEmpty("")
                .flatMap(body -> {
                    if (status == HttpStatus.NOT_FOUND) {
                        return Mono.error(new ResourceNotFoundException(body));
                    } else if (status == HttpStatus.UNPROCESSABLE_ENTITY) {
                        return Mono.error(new DomainException(body));
                    } else {
                        // Preserva el código de estado real (401, 403, 500, etc.)
                        return Mono.error(new ResponseStatusException(status, body));
                    }
                });
    }
}