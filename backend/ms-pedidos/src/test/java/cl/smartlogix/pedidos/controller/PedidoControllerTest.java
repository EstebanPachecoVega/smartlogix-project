package cl.smartlogix.pedidos.controller;

import cl.smartlogix.pedidos.config.TestSecurityConfig;
import cl.smartlogix.pedidos.dto.request.CrearPedidoRequestDTO;
import cl.smartlogix.pedidos.dto.response.*;
import cl.smartlogix.pedidos.entity.Pedido;
import cl.smartlogix.pedidos.mapper.PedidoMapper;
import cl.smartlogix.pedidos.repository.DetallePedidoRepository;
import cl.smartlogix.pedidos.repository.PedidoRepository;
import cl.smartlogix.pedidos.service.PedidoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.securityContext;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PedidoController.class)
@Import(TestSecurityConfig.class)
class PedidoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PedidoService pedidoService;

    @MockitoBean
    private PedidoMapper pedidoMapper;

    @MockitoBean
    private PedidoRepository pedidoRepository;

    @MockitoBean
    private DetallePedidoRepository detallePedidoRepository;

    private static SecurityContext gestorContext() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .claim("sub", "gestor123")
                .claim("realm_access", Map.of("roles", List.of("gestor")))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
        JwtAuthenticationToken auth = new JwtAuthenticationToken(jwt, List.of(() -> "ROLE_gestor"));
        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(auth);
        return ctx;
    }

    private static SecurityContext userContext() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .claim("sub", "user123")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
        JwtAuthenticationToken auth = new JwtAuthenticationToken(jwt, List.of(() -> "ROLE_user"));
        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(auth);
        return ctx;
    }

    private static SecurityContext userContextRealmAccessSinRoles() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .claim("sub", "user123")
                .claim("realm_access", Map.of())
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
        JwtAuthenticationToken auth = new JwtAuthenticationToken(jwt, List.of(() -> "ROLE_user"));
        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(auth);
        return ctx;
    }

    private static SecurityContext userContextRealmAccessSinGestor() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .claim("sub", "user123")
                .claim("realm_access", Map.of("roles", List.of("user")))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
        JwtAuthenticationToken auth = new JwtAuthenticationToken(jwt, List.of(() -> "ROLE_user"));
        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(auth);
        return ctx;
    }

    @Test
    void crearPedido_201() throws Exception {
        CrearPedidoRequestDTO request = crearRequestValido();
        Pedido pedido = new Pedido();
        PedidoResponseDTO response = new PedidoResponseDTO();
        response.setId(1L);
        response.setNumeroOrden("ORD-001");

        when(pedidoService.crearPedido(any(), anyString(), anyString())).thenReturn(pedido);
        when(pedidoMapper.toResponseDTO(pedido)).thenReturn(response);

        mockMvc.perform(post("/api/pedidos")
                        .with(securityContext(userContext()))
                        .with(csrf())
                        .header("Idempotency-Key", "key-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.numeroOrden").value("ORD-001"));
    }

    @Test
    void crearPedido_sinItems_400() throws Exception {
        mockMvc.perform(post("/api/pedidos")
                        .with(securityContext(userContext()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuarioId\":\"u1\",\"destinatario\":\"Test\",\"calle\":\"Calle\",\"numero\":\"123\",\"comuna\":\"Santiago\",\"ciudad\":\"Santiago\",\"metodoEnvio\":\"Despacho\",\"items\":[]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listarPedidos_comoGestor_200() throws Exception {
        Pedido pedido = new Pedido();
        PedidoResponseDTO dto = new PedidoResponseDTO();
        dto.setId(1L);

        when(pedidoService.listarPedidos()).thenReturn(List.of(pedido));
        when(pedidoMapper.toResponseDTO(pedido)).thenReturn(dto);

        mockMvc.perform(get("/api/pedidos")
                        .with(securityContext(gestorContext())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void listarPedidos_comoUsuario_200() throws Exception {
        Pedido pedido = new Pedido();
        PedidoResponseDTO dto = new PedidoResponseDTO();
        dto.setId(1L);

        when(pedidoService.listarPedidosPorUsuario("user123")).thenReturn(List.of(pedido));
        when(pedidoMapper.toResponseDTO(pedido)).thenReturn(dto);

        mockMvc.perform(get("/api/pedidos")
                        .with(securityContext(userContext())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void obtenerPedidoPorId_comoGestor_200() throws Exception {
        Pedido pedido = new Pedido();
        pedido.setUsuarioId("otro-user");
        PedidoResponseDTO dto = new PedidoResponseDTO();
        dto.setId(1L);

        when(pedidoService.obtenerPedidoPorId(1L)).thenReturn(pedido);
        when(pedidoMapper.toResponseDTO(pedido)).thenReturn(dto);

        mockMvc.perform(get("/api/pedidos/1")
                        .with(securityContext(gestorContext())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void obtenerPedidoPorId_otroUsuario_lanzaResponseStatusException() {
        Pedido pedido = new Pedido();
        pedido.setUsuarioId("otro-user");

        PedidoController controller = new PedidoController(
                pedidoService, pedidoMapper, pedidoRepository, detallePedidoRepository);

        when(pedidoService.obtenerPedidoPorId(1L)).thenReturn(pedido);

        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .claim("sub", "user123")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.obtenerPedidoPorId(1L, jwt));
        assertEquals(403, ex.getStatusCode().value());
    }

    @Test
    void obtenerPedidoPorNumeroOrden_200() throws Exception {
        Pedido pedido = new Pedido();
        pedido.setUsuarioId("user123");
        PedidoResponseDTO dto = new PedidoResponseDTO();
        dto.setId(1L);

        when(pedidoService.obtenerPedidoPorNumeroOrden("ORD-001")).thenReturn(pedido);
        when(pedidoMapper.toResponseDTO(pedido)).thenReturn(dto);

        mockMvc.perform(get("/api/pedidos/orden/ORD-001")
                        .with(securityContext(userContext())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getVentasPorPlataforma_200() throws Exception {
        VentasPlataformaResponseDTO v1 = new VentasPlataformaResponseDTO("DESKTOP", 50000L);
        VentasPlataformaResponseDTO v2 = new VentasPlataformaResponseDTO("MOBILE", 30000L);

        when(pedidoRepository.findVentasPorPlataforma()).thenReturn(List.of(v1, v2));

        mockMvc.perform(get("/api/pedidos/estadisticas/ventas-plataforma")
                        .with(securityContext(gestorContext())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].plataforma").value("DESKTOP"))
                .andExpect(jsonPath("$[0].total").value(50000));
    }

    @Test
    void getComparacionAnual_200() throws Exception {
        ComparacionAnualResponseDTO c1 = new ComparacionAnualResponseDTO(1, 10000L, 8000L);

        when(pedidoRepository.findComparacionAnual(anyInt(), anyInt())).thenReturn(List.of(c1));

        mockMvc.perform(get("/api/pedidos/estadisticas/comparacion-anual")
                        .with(securityContext(gestorContext())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].mes").value(1))
                .andExpect(jsonPath("$[0].añoActual").value(10000));
    }

    @Test
    void getVentasPorProducto_200() throws Exception {
        VentaPorProductoResponseDTO v = new VentaPorProductoResponseDTO(1L, 50000L);

        when(detallePedidoRepository.findVentasPorProducto()).thenReturn(List.of(v));

        mockMvc.perform(get("/api/pedidos/estadisticas/ventas-por-producto")
                        .with(securityContext(gestorContext())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].productoId").value(1));
    }

    @Test
    void getCantidadPorProducto_200() throws Exception {
        VentaPorProductoCantidadDTO v = new VentaPorProductoCantidadDTO(1L, 10L);

        when(detallePedidoRepository.findCantidadPorProducto()).thenReturn(List.of(v));

        mockMvc.perform(get("/api/pedidos/estadisticas/ventas-por-producto-cantidad")
                        .with(securityContext(gestorContext())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].cantidad").value(10));
    }

    // ---------- FORBIDDEN when non-gestor accesses another user's pedido ----------

    @Test
    void obtenerPedidoPorId_otroUsuario_forbidden() throws Exception {
        Pedido pedido = new Pedido();
        pedido.setUsuarioId("otro-user");

        when(pedidoService.obtenerPedidoPorId(1L)).thenReturn(pedido);

        mockMvc.perform(get("/api/pedidos/1")
                        .with(securityContext(userContext())))
                .andExpect(status().isForbidden());
    }

    @Test
    void obtenerPedidoPorNumeroOrden_otroUsuario_forbidden() throws Exception {
        Pedido pedido = new Pedido();
        pedido.setUsuarioId("otro-user");

        when(pedidoService.obtenerPedidoPorNumeroOrden("ORD-001")).thenReturn(pedido);

        mockMvc.perform(get("/api/pedidos/orden/ORD-001")
                        .with(securityContext(userContext())))
                .andExpect(status().isForbidden());
    }

    // ---------- esGestor branch coverage ----------

    @Test
    void obtenerPedidoPorId_realmAccessSinRoles_esGestorFalse() throws Exception {
        Pedido pedido = new Pedido();
        pedido.setUsuarioId("otro-user");

        when(pedidoService.obtenerPedidoPorId(1L)).thenReturn(pedido);

        mockMvc.perform(get("/api/pedidos/1")
                        .with(securityContext(userContextRealmAccessSinRoles())))
                .andExpect(status().isForbidden());
    }

    @Test
    void obtenerPedidoPorId_realmAccessSinGestor_esGestorFalse() throws Exception {
        Pedido pedido = new Pedido();
        pedido.setUsuarioId("otro-user");

        when(pedidoService.obtenerPedidoPorId(1L)).thenReturn(pedido);

        mockMvc.perform(get("/api/pedidos/1")
                        .with(securityContext(userContextRealmAccessSinGestor())))
                .andExpect(status().isForbidden());
    }

    @Test
    void obtenerPedidoPorId_comoUsuarioPropio_200() throws Exception {
        Pedido pedido = new Pedido();
        pedido.setUsuarioId("user123");
        PedidoResponseDTO dto = new PedidoResponseDTO();
        dto.setId(1L);

        when(pedidoService.obtenerPedidoPorId(1L)).thenReturn(pedido);
        when(pedidoMapper.toResponseDTO(pedido)).thenReturn(dto);

        mockMvc.perform(get("/api/pedidos/1")
                        .with(securityContext(userContext())))
                .andExpect(status().isOk());
    }

    @Test
    void obtenerPedidoPorNumeroOrden_comoGestor_200() throws Exception {
        Pedido pedido = new Pedido();
        pedido.setUsuarioId("otro-user");
        PedidoResponseDTO dto = new PedidoResponseDTO();
        dto.setId(1L);

        when(pedidoService.obtenerPedidoPorNumeroOrden("ORD-001")).thenReturn(pedido);
        when(pedidoMapper.toResponseDTO(pedido)).thenReturn(dto);

        mockMvc.perform(get("/api/pedidos/orden/ORD-001")
                        .with(securityContext(gestorContext())))
                .andExpect(status().isOk());
    }

    @Test
    void obtenerPedidoPorId_comoGestor_sobrePedidoAjeno_ok() throws Exception {
        Pedido pedido = new Pedido();
        pedido.setUsuarioId("otro-user");
        PedidoResponseDTO dto = new PedidoResponseDTO();
        dto.setId(1L);

        when(pedidoService.obtenerPedidoPorId(1L)).thenReturn(pedido);
        when(pedidoMapper.toResponseDTO(pedido)).thenReturn(dto);

        mockMvc.perform(get("/api/pedidos/1")
                        .with(securityContext(gestorContext())))
                .andExpect(status().isOk());
    }

    private CrearPedidoRequestDTO crearRequestValido() {
        CrearPedidoRequestDTO req = new CrearPedidoRequestDTO();
        req.setUsuarioId("user123");
        req.setDestinatario("Juan Pérez");
        req.setCalle("Av. Siempre Viva");
        req.setNumero("742");
        req.setComuna("Santiago");
        req.setCiudad("Santiago");
        req.setMetodoEnvio("Despacho");
        CrearPedidoRequestDTO.DetalleRequestDTO item = new CrearPedidoRequestDTO.DetalleRequestDTO();
        item.setProductoId(1L);
        item.setSku("SKU-001");
        item.setNombreProducto("Producto Test");
        item.setPrecioUnitario(5000);
        item.setCantidad(2);
        req.setItems(List.of(item));
        return req;
    }
}
