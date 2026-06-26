package cl.smartlogix.bff.client;

import cl.smartlogix.bff.config.ReactiveCacheManager;
import cl.smartlogix.bff.dto.request.*;
import cl.smartlogix.bff.dto.response.*;
import cl.smartlogix.bff.exception.DomainException;
import cl.smartlogix.bff.exception.ResourceNotFoundException;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@WireMockTest(httpPort = 18080)
class GatewayClientTest {

    @Autowired
    private WebClient.Builder webClientBuilder;

    private GatewayClient client;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("gateway.url", () -> "http://localhost:18080");
    }

    @BeforeEach
    void setUp() {
        WebClient webClient = webClientBuilder.baseUrl("http://localhost:18080").build();
        client = new GatewayClient(webClient, new ReactiveCacheManager());
    }

    // ==================== PRODUCTOS ====================

    @Test
    void getProductos_returnsList() {
        stubFor(get(urlPathEqualTo("/api/productos"))
                .withQueryParam("page", equalTo("0"))
                .withQueryParam("size", equalTo("10"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"content\":[{\"id\":1,\"nombre\":\"Producto 1\"}],\"totalPages\":1,\"totalElements\":1,\"number\":0,\"size\":10}")));

        PagedResponse<ProductoResponseDTO> result = client.getProductos("test-jwt", "cid-123", 0, 10).block();

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getNombre()).isEqualTo("Producto 1");
    }

    @Test
    void crearProducto_returnsProducto() {
        ProductoRequestDTO request = new ProductoRequestDTO();
        request.setNombre("Nuevo Producto");

        stubFor(post(urlEqualTo("/api/productos"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":1,\"nombre\":\"Nuevo Producto\"}")));

        ProductoResponseDTO result = client.crearProducto(request, "test-jwt", "cid-123").block();

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNombre()).isEqualTo("Nuevo Producto");
    }

    @Test
    void obtenerProducto_returnsProducto() {
        stubFor(get(urlEqualTo("/api/productos/1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":1,\"nombre\":\"Producto 1\"}")));

        ProductoResponseDTO result = client.obtenerProducto(1L, "test-jwt", "cid-123").block();

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void actualizarProducto_returnsProducto() {
        ProductoRequestDTO request = new ProductoRequestDTO();
        request.setNombre("Actualizado");

        stubFor(put(urlEqualTo("/api/productos/1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":1,\"nombre\":\"Actualizado\"}")));

        ProductoResponseDTO result = client.actualizarProducto(1L, request, "test-jwt", "cid-123").block();

        assertThat(result).isNotNull();
        assertThat(result.getNombre()).isEqualTo("Actualizado");
    }

    @Test
    void eliminarProducto_returnsVoid() {
        stubFor(delete(urlEqualTo("/api/productos/1"))
                .willReturn(aResponse().withStatus(200)));

        Void result = client.eliminarProducto(1L, "test-jwt", "cid-123").block();

        assertThat(result).isNull();
    }

    // ==================== CATEGORÍAS ====================

    @Test
    void listarCategorias_returnsList() {
        stubFor(get(urlPathEqualTo("/api/categorias"))
                .withQueryParam("page", equalTo("0"))
                .withQueryParam("size", equalTo("10"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"content\":[{\"id\":1,\"nombre\":\"Categoria 1\"}],\"totalPages\":1,\"totalElements\":1,\"number\":0,\"size\":10}")));

        PagedResponse<CategoriaResponseDTO> result = client.listarCategorias("test-jwt", "cid-123", 0, 10).block();

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getNombre()).isEqualTo("Categoria 1");
    }

    @Test
    void obtenerCategoria_returnsCategoria() {
        stubFor(get(urlEqualTo("/api/categorias/1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":1,\"nombre\":\"Categoria 1\"}")));

        CategoriaResponseDTO result = client.obtenerCategoria(1L, "test-jwt", "cid-123").block();

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void crearCategoria_returnsCategoria() {
        CategoriaRequestDTO request = new CategoriaRequestDTO();
        request.setNombre("Nueva Categoria");

        stubFor(post(urlEqualTo("/api/categorias"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":1,\"nombre\":\"Nueva Categoria\"}")));

        CategoriaResponseDTO result = client.crearCategoria(request, "test-jwt", "cid-123").block();

        assertThat(result).isNotNull();
        assertThat(result.getNombre()).isEqualTo("Nueva Categoria");
    }

    @Test
    void actualizarCategoria_returnsCategoria() {
        CategoriaRequestDTO request = new CategoriaRequestDTO();
        request.setNombre("Actualizada");

        stubFor(put(urlEqualTo("/api/categorias/1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":1,\"nombre\":\"Actualizada\"}")));

        CategoriaResponseDTO result = client.actualizarCategoria(1L, request, "test-jwt", "cid-123").block();

        assertThat(result).isNotNull();
        assertThat(result.getNombre()).isEqualTo("Actualizada");
    }

    @Test
    void eliminarCategoria_returnsVoid() {
        stubFor(delete(urlEqualTo("/api/categorias/1"))
                .willReturn(aResponse().withStatus(200)));

        Void result = client.eliminarCategoria(1L, "test-jwt", "cid-123").block();

        assertThat(result).isNull();
    }

    @Test
    void reordenarCategorias_returnsVoid() {
        ReordenarCategoriaDTO orden = new ReordenarCategoriaDTO(1L, 1);

        stubFor(patch(urlEqualTo("/api/categorias/reordenar"))
                .willReturn(aResponse().withStatus(200)));

        Void result = client.reordenarCategorias(List.of(orden), "test-jwt", "cid-123").block();

        assertThat(result).isNull();
    }

    // ==================== PEDIDOS ====================

    @Test
    void crearPedido_withIdempotencyKey() {
        CrearPedidoRequestDTO request = new CrearPedidoRequestDTO();
        request.setUsuarioId(1L);

        stubFor(post(urlEqualTo("/api/pedidos"))
                .withHeader("Idempotency-Key", equalTo("key-123"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":1,\"numeroOrden\":\"ORD-001\"}")));

        PedidoResponseDTO result = client.crearPedido(request, "test-jwt", "key-123", "cid-123").block();

        assertThat(result).isNotNull();
        assertThat(result.getNumeroOrden()).isEqualTo("ORD-001");
    }

    @Test
    void crearPedido_withoutIdempotencyKey() {
        CrearPedidoRequestDTO request = new CrearPedidoRequestDTO();
        request.setUsuarioId(1L);

        stubFor(post(urlEqualTo("/api/pedidos"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":1,\"numeroOrden\":\"ORD-002\"}")));

        PedidoResponseDTO result = client.crearPedido(request, "test-jwt", null, "cid-123").block();

        assertThat(result).isNotNull();
        assertThat(result.getNumeroOrden()).isEqualTo("ORD-002");
    }

    @Test
    void listarPedidos_returnsList() {
        stubFor(get(urlPathEqualTo("/api/pedidos"))
                .withQueryParam("page", equalTo("0"))
                .withQueryParam("size", equalTo("10"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"content\":[{\"id\":1,\"numeroOrden\":\"ORD-001\"}],\"totalPages\":1,\"totalElements\":1,\"number\":0,\"size\":10}")));

        PagedResponse<PedidoResponseDTO> result = client.listarPedidos("test-jwt", "cid-123", 0, 10).block();

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getNumeroOrden()).isEqualTo("ORD-001");
    }

    @Test
    void obtenerPedido_returnsPedido() {
        stubFor(get(urlEqualTo("/api/pedidos/1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":1,\"numeroOrden\":\"ORD-001\"}")));

        PedidoResponseDTO result = client.obtenerPedido(1L, "test-jwt", "cid-123").block();

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    // ==================== ENVÍOS ====================

    @Test
    void listarEnvios_returnsList() {
        stubFor(get(urlPathEqualTo("/api/envios"))
                .withQueryParam("page", equalTo("0"))
                .withQueryParam("size", equalTo("10"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"content\":[{\"id\":1,\"numeroTracking\":\"TRK-001\"}],\"totalPages\":1,\"totalElements\":1,\"number\":0,\"size\":10}")));

        PagedResponse<EnvioResponseDTO> result = client.listarEnvios("test-jwt", "cid-123", 0, 10).block();

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getNumeroTracking()).isEqualTo("TRK-001");
    }

    @Test
    void obtenerEnvio_returnsEnvio() {
        stubFor(get(urlEqualTo("/api/envios/1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":1,\"numeroTracking\":\"TRK-001\"}")));

        EnvioResponseDTO result = client.obtenerEnvio(1L, "test-jwt", "cid-123").block();

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void obtenerEnvioPorPedidoId_returnsEnvio() {
        stubFor(get(urlEqualTo("/api/envios/pedido/1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":1,\"pedidoId\":1,\"numeroTracking\":\"TRK-001\"}")));

        EnvioResponseDTO result = client.obtenerEnvioPorPedidoId(1L, "test-jwt", "cid-123").block();

        assertThat(result).isNotNull();
        assertThat(result.getPedidoId()).isEqualTo(1L);
    }

    @Test
    void actualizarEstadoEnvio_returnsEnvio() {
        stubFor(patch(urlEqualTo("/api/envios/1/estado?nuevoEstado=ENVIADO"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":1,\"estadoEnvio\":\"ENVIADO\"}")));

        EnvioResponseDTO result = client.actualizarEstadoEnvio(1L, "ENVIADO", "test-jwt", "cid-123").block();

        assertThat(result).isNotNull();
        assertThat(result.getEstadoEnvio()).isEqualTo("ENVIADO");
    }

    @Test
    void obtenerEnvioPorTracking_returnsEnvio() {
        stubFor(get(urlEqualTo("/api/envios/tracking/TRK-001"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":1,\"numeroTracking\":\"TRK-001\"}")));

        EnvioResponseDTO result = client.obtenerEnvioPorTracking("TRK-001", "test-jwt", "cid-123").block();

        assertThat(result).isNotNull();
        assertThat(result.getNumeroTracking()).isEqualTo("TRK-001");
    }

    // ==================== ESTADÍSTICAS ====================

    @Test
    void getVentasPlataforma_returnsList() {
        stubFor(get(urlEqualTo("/api/pedidos/estadisticas/ventas-plataforma"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[{\"plataforma\":\"DESKTOP\",\"total\":10}]")));

        List<VentasPlataformaResponseDTO> result = client.getVentasPlataforma("test-jwt", "cid-123").block();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPlataforma()).isEqualTo("DESKTOP");
    }

    @Test
    void getComparacionAnual_returnsList() {
        stubFor(get(urlEqualTo("/api/pedidos/estadisticas/comparacion-anual"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[{\"mes\":1,\"añoActual\":100,\"añoAnterior\":80}]")));

        List<ComparacionAnualResponseDTO> result = client.getComparacionAnual("test-jwt", "cid-123").block();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMes()).isEqualTo(1);
    }

    @Test
    void getVentasPorProducto_returnsList() {
        stubFor(get(urlEqualTo("/api/pedidos/estadisticas/ventas-por-producto"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[{\"productoId\":1,\"totalVentas\":50}]")));

        List<VentaPorProductoResponseDTO> result = client.getVentasPorProducto("test-jwt", "cid-123").block();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProductoId()).isEqualTo(1L);
    }

    @Test
    void getCantidadPorProducto_returnsList() {
        stubFor(get(urlEqualTo("/api/pedidos/estadisticas/ventas-por-producto-cantidad"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[{\"productoId\":1,\"cantidad\":100}]")));

        List<VentaPorProductoCantidadDTO> result = client.getCantidadPorProducto("test-jwt", "cid-123").block();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProductoId()).isEqualTo(1L);
    }

    @Test
    void getMapaCategorias_returnsList() {
        stubFor(get(urlEqualTo("/api/productos/mapa-categorias"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[{\"productoId\":1,\"categoriaNombre\":\"Electrónicos\"}]")));

        List<MapaCategoriaResponseDTO> result = client.getMapaCategorias("test-jwt", "cid-123").block();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCategoriaNombre()).isEqualTo("Electrónicos");
    }

    // ==================== ERROR HANDLING ====================

    @Test
    void handleError_404_throwsResourceNotFoundException() {
        stubFor(get(urlEqualTo("/api/productos/999"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withBody("Producto no encontrado")));

        assertThrows(ResourceNotFoundException.class, () -> {
            client.obtenerProducto(999L, "test-jwt", "cid-123").block();
        });
    }

    @Test
    void handleError_422_throwsDomainException() {
        CrearPedidoRequestDTO request = new CrearPedidoRequestDTO();
        request.setUsuarioId(1L);

        stubFor(post(urlEqualTo("/api/pedidos"))
                .willReturn(aResponse()
                        .withStatus(422)
                        .withBody("Datos inválidos")));

        assertThrows(DomainException.class, () -> {
            client.crearPedido(request, "test-jwt", "key-123", "cid-123").block();
        });
    }

    @Test
    void handleError_500_throwsResponseStatusException() {
        stubFor(get(urlPathEqualTo("/api/productos"))
                .withQueryParam("page", equalTo("0"))
                .withQueryParam("size", equalTo("10"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("Error interno del servidor")));

        try {
            client.getProductos("test-jwt", "cid-123", 0, 10).block();
        } catch (ResponseStatusException e) {
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(e.getReason()).contains("Error interno del servidor");
        }
    }

    // ==================== FALLBACK METHODS (via reflection) ====================

    @Test
    void fallbackGetProductos_returnsServiceUnavailable() throws Exception {
        Method method = GatewayClient.class.getDeclaredMethod("fallbackGetProductos", String.class, String.class, int.class, int.class, Throwable.class);
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        Mono<PagedResponse<ProductoResponseDTO>> result = (Mono<PagedResponse<ProductoResponseDTO>>) method.invoke(client, "jwt", "cid", 0, 10, new RuntimeException("test"));
        assertFallbackError(result);
    }

    @Test
    void fallbackCrearProducto_returnsServiceUnavailable() throws Exception {
        Method method = GatewayClient.class.getDeclaredMethod("fallbackCrearProducto", ProductoRequestDTO.class, String.class, String.class, Throwable.class);
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        Mono<ProductoResponseDTO> result = (Mono<ProductoResponseDTO>) method.invoke(client, new ProductoRequestDTO(), "jwt", "cid", new RuntimeException("test"));
        assertFallbackError(result);
    }

    @Test
    void fallbackObtenerProducto_returnsServiceUnavailable() throws Exception {
        Method method = GatewayClient.class.getDeclaredMethod("fallbackObtenerProducto", Long.class, String.class, String.class, Throwable.class);
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        Mono<ProductoResponseDTO> result = (Mono<ProductoResponseDTO>) method.invoke(client, 1L, "jwt", "cid", new RuntimeException("test"));
        assertFallbackError(result);
    }

    @Test
    void fallbackActualizarProducto_returnsServiceUnavailable() throws Exception {
        Method method = GatewayClient.class.getDeclaredMethod("fallbackActualizarProducto", Long.class, ProductoRequestDTO.class, String.class, String.class, Throwable.class);
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        Mono<ProductoResponseDTO> result = (Mono<ProductoResponseDTO>) method.invoke(client, 1L, new ProductoRequestDTO(), "jwt", "cid", new RuntimeException("test"));
        assertFallbackError(result);
    }

    @Test
    void fallbackEliminarProducto_returnsServiceUnavailable() throws Exception {
        Method method = GatewayClient.class.getDeclaredMethod("fallbackEliminarProducto", Long.class, String.class, String.class, Throwable.class);
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        Mono<Void> result = (Mono<Void>) method.invoke(client, 1L, "jwt", "cid", new RuntimeException("test"));
        assertFallbackError(result);
    }

    @Test
    void fallbackListarCategorias_returnsServiceUnavailable() throws Exception {
        Method method = GatewayClient.class.getDeclaredMethod("fallbackListarCategorias", String.class, String.class, int.class, int.class, Throwable.class);
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        Mono<PagedResponse<CategoriaResponseDTO>> result = (Mono<PagedResponse<CategoriaResponseDTO>>) method.invoke(client, "jwt", "cid", 0, 10, new RuntimeException("test"));
        assertFallbackError(result);
    }

    @Test
    void fallbackCrearCategoria_returnsServiceUnavailable() throws Exception {
        Method method = GatewayClient.class.getDeclaredMethod("fallbackCrearCategoria", CategoriaRequestDTO.class, String.class, String.class, Throwable.class);
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        Mono<CategoriaResponseDTO> result = (Mono<CategoriaResponseDTO>) method.invoke(client, new CategoriaRequestDTO(), "jwt", "cid", new RuntimeException("test"));
        assertFallbackError(result);
    }

    @Test
    void fallbackActualizarCategoria_returnsServiceUnavailable() throws Exception {
        Method method = GatewayClient.class.getDeclaredMethod("fallbackActualizarCategoria", Long.class, CategoriaRequestDTO.class, String.class, String.class, Throwable.class);
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        Mono<CategoriaResponseDTO> result = (Mono<CategoriaResponseDTO>) method.invoke(client, 1L, new CategoriaRequestDTO(), "jwt", "cid", new RuntimeException("test"));
        assertFallbackError(result);
    }

    @Test
    void fallbackEliminarCategoria_returnsServiceUnavailable() throws Exception {
        Method method = GatewayClient.class.getDeclaredMethod("fallbackEliminarCategoria", Long.class, String.class, String.class, Throwable.class);
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        Mono<Void> result = (Mono<Void>) method.invoke(client, 1L, "jwt", "cid", new RuntimeException("test"));
        assertFallbackError(result);
    }

    @Test
    void fallbackReordenarCategorias_returnsServiceUnavailable() throws Exception {
        Method method = GatewayClient.class.getDeclaredMethod("fallbackReordenarCategorias", List.class, String.class, String.class, Throwable.class);
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        Mono<Void> result = (Mono<Void>) method.invoke(client, List.of(), "jwt", "cid", new RuntimeException("test"));
        assertFallbackError(result);
    }

    @Test
    void fallbackCrearPedido_returnsServiceUnavailable() throws Exception {
        Method method = GatewayClient.class.getDeclaredMethod("fallbackCrearPedido", CrearPedidoRequestDTO.class, String.class, String.class, Throwable.class);
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        Mono<PedidoResponseDTO> result = (Mono<PedidoResponseDTO>) method.invoke(client, new CrearPedidoRequestDTO(), "jwt", "cid", new RuntimeException("test"));
        assertFallbackError(result);
    }

    @Test
    void fallbackListarPedidos_returnsServiceUnavailable() throws Exception {
        Method method = GatewayClient.class.getDeclaredMethod("fallbackListarPedidos", String.class, String.class, int.class, int.class, Throwable.class);
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        Mono<PagedResponse<PedidoResponseDTO>> result = (Mono<PagedResponse<PedidoResponseDTO>>) method.invoke(client, "jwt", "cid", 0, 10, new RuntimeException("test"));
        assertFallbackError(result);
    }

    @Test
    void fallbackObtenerPedido_returnsServiceUnavailable() throws Exception {
        Method method = GatewayClient.class.getDeclaredMethod("fallbackObtenerPedido", Long.class, String.class, String.class, Throwable.class);
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        Mono<PedidoResponseDTO> result = (Mono<PedidoResponseDTO>) method.invoke(client, 1L, "jwt", "cid", new RuntimeException("test"));
        assertFallbackError(result);
    }

    @Test
    void fallbackListarEnvios_returnsServiceUnavailable() throws Exception {
        Method method = GatewayClient.class.getDeclaredMethod("fallbackListarEnvios", String.class, String.class, int.class, int.class, Throwable.class);
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        Mono<PagedResponse<EnvioResponseDTO>> result = (Mono<PagedResponse<EnvioResponseDTO>>) method.invoke(client, "jwt", "cid", 0, 10, new RuntimeException("test"));
        assertFallbackError(result);
    }

    @Test
    void fallbackObtenerEnvio_returnsServiceUnavailable() throws Exception {
        Method method = GatewayClient.class.getDeclaredMethod("fallbackObtenerEnvio", Long.class, String.class, String.class, Throwable.class);
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        Mono<EnvioResponseDTO> result = (Mono<EnvioResponseDTO>) method.invoke(client, 1L, "jwt", "cid", new RuntimeException("test"));
        assertFallbackError(result);
    }

    @Test
    void fallbackObtenerEnvioPorPedidoId_returnsServiceUnavailable() throws Exception {
        Method method = GatewayClient.class.getDeclaredMethod("fallbackObtenerEnvioPorPedidoId", Long.class, String.class, String.class, Throwable.class);
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        Mono<EnvioResponseDTO> result = (Mono<EnvioResponseDTO>) method.invoke(client, 1L, "jwt", "cid", new RuntimeException("test"));
        assertFallbackError(result);
    }

    @Test
    void fallbackActualizarEstadoEnvio_returnsServiceUnavailable() throws Exception {
        Method method = GatewayClient.class.getDeclaredMethod("fallbackActualizarEstadoEnvio", Long.class, String.class, String.class, String.class, Throwable.class);
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        Mono<EnvioResponseDTO> result = (Mono<EnvioResponseDTO>) method.invoke(client, 1L, "ENVIADO", "jwt", "cid", new RuntimeException("test"));
        assertFallbackError(result);
    }

    @Test
    void fallbackObtenerEnvioPorTracking_returnsServiceUnavailable() throws Exception {
        Method method = GatewayClient.class.getDeclaredMethod("fallbackObtenerEnvioPorTracking", String.class, String.class, String.class, Throwable.class);
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        Mono<EnvioResponseDTO> result = (Mono<EnvioResponseDTO>) method.invoke(client, "TRK", "jwt", "cid", new RuntimeException("test"));
        assertFallbackError(result);
    }

    @Test
    void fallbackEstadisticas_returnsServiceUnavailable() throws Exception {
        Method method = GatewayClient.class.getDeclaredMethod("fallbackEstadisticas", String.class, String.class, Throwable.class);
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        Mono<List<Object>> result = (Mono<List<Object>>) method.invoke(client, "jwt", "cid", new RuntimeException("test"));
        assertFallbackError(result);
    }

    private void assertFallbackError(Mono<?> result) {
        assertThrows(ResponseStatusException.class, () -> {
            try {
                result.block();
            } catch (ResponseStatusException e) {
                assertThat(e.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
                throw e;
            }
        });
    }
}
