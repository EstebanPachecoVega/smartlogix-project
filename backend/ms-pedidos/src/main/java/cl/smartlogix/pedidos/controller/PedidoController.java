package cl.smartlogix.pedidos.controller;

import cl.smartlogix.pedidos.dto.request.CrearPedidoRequestDTO;
import cl.smartlogix.pedidos.dto.response.ComparacionAnualResponseDTO;
import cl.smartlogix.pedidos.dto.response.PedidoResponseDTO;
import cl.smartlogix.pedidos.dto.response.VentaPorProductoCantidadDTO;
import cl.smartlogix.pedidos.dto.response.VentaPorProductoResponseDTO;
import cl.smartlogix.pedidos.dto.response.VentasPlataformaResponseDTO;
import cl.smartlogix.pedidos.entity.EstadoPedido;
import cl.smartlogix.pedidos.entity.Pedido;
import cl.smartlogix.pedidos.mapper.PedidoMapper;
import cl.smartlogix.pedidos.repository.DetallePedidoRepository;
import cl.smartlogix.pedidos.repository.PedidoRepository;
import cl.smartlogix.pedidos.service.PedidoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;
    private final PedidoMapper pedidoMapper;
    private final PedidoRepository pedidoRepository;
    private final DetallePedidoRepository detallePedidoRepository;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PedidoResponseDTO crearPedido(
            @Valid @RequestBody CrearPedidoRequestDTO request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @AuthenticationPrincipal Jwt jwt) {
        // Extraer el sub (UUID) del token como String
        String usuarioId = jwt.getClaimAsString("sub");
        Pedido pedido = pedidoService.crearPedido(request, usuarioId, idempotencyKey);
        return pedidoMapper.toResponseDTO(pedido);
    }

    @GetMapping
    public Page<PedidoResponseDTO> listarPedidos(
            @AuthenticationPrincipal Jwt jwt,
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(required = false) EstadoPedido estado) {
        String usuarioId = jwt.getClaimAsString("sub");
        boolean isGestor = esGestor(jwt);

        Page<Pedido> pedidos;
        if (isGestor) {
            pedidos = (estado != null)
                    ? pedidoService.listarPedidos(pageable, estado)
                    : pedidoService.listarPedidos(pageable);
        } else {
            pedidos = pedidoService.listarPedidosPorUsuario(usuarioId, pageable);
        }
        return pedidos.map(pedidoMapper::toResponseDTO);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarPedido(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        if (!esGestor(jwt)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo gestores pueden eliminar pedidos");
        }
        pedidoService.deletePedido(id);
    }

    @GetMapping("/{id}")
    public PedidoResponseDTO obtenerPedidoPorId(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        String usuarioId = jwt.getClaimAsString("sub");
        boolean isGestor = esGestor(jwt);

        Pedido pedido = pedidoService.obtenerPedidoPorId(id);
        if (!isGestor && !pedido.getUsuarioId().equals(usuarioId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para ver este pedido");
        }
        return pedidoMapper.toResponseDTO(pedido);
    }

    @GetMapping("/orden/{numeroOrden}")
    public PedidoResponseDTO obtenerPedidoPorNumeroOrden(@PathVariable String numeroOrden,
            @AuthenticationPrincipal Jwt jwt) {
        String usuarioId = jwt.getClaimAsString("sub");
        boolean isGestor = esGestor(jwt);

        Pedido pedido = pedidoService.obtenerPedidoPorNumeroOrden(numeroOrden);
        if (!isGestor && !pedido.getUsuarioId().equals(usuarioId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para ver este pedido");
        }
        return pedidoMapper.toResponseDTO(pedido);
    }

    // ==================== ESTADÍSTICAS / AGREGACIÓN ====================

    @GetMapping("/estadisticas/ventas-plataforma")
    public List<VentasPlataformaResponseDTO> getVentasPorPlataforma() {
        LocalDateTime desde = LocalDate.now().minusMonths(12).atStartOfDay();
        return pedidoRepository.findVentasPorPlataforma(desde);
    }

    @GetMapping("/estadisticas/comparacion-anual")
    public List<ComparacionAnualResponseDTO> getComparacionAnual() {
        int anioActual = LocalDate.now().getYear();
        int anioAnterior = anioActual - 1;
        return pedidoRepository.findComparacionAnual(anioActual, anioAnterior);
    }

    @GetMapping("/estadisticas/ventas-por-producto")
    public List<VentaPorProductoResponseDTO> getVentasPorProducto() {
        LocalDateTime desde = LocalDate.now().minusMonths(12).atStartOfDay();
        return detallePedidoRepository.findVentasPorProducto(desde);
    }

    @GetMapping("/estadisticas/ventas-por-producto-cantidad")
    public List<VentaPorProductoCantidadDTO> getCantidadPorProducto() {
        LocalDateTime desde = LocalDate.now().minusMonths(12).atStartOfDay();
        return detallePedidoRepository.findCantidadPorProducto(desde);
    }

    // Método helper — agrega esto al final de la clase
    @SuppressWarnings("unchecked")
    private boolean esGestor(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess == null)
            return false;
        List<String> roles = (List<String>) realmAccess.get("roles");
        return roles != null && roles.contains("gestor");
    }
}