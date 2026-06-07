package cl.smartlogix.pedidos.controller;

import cl.smartlogix.pedidos.dto.request.CrearPedidoRequestDTO;
import cl.smartlogix.pedidos.dto.response.PedidoResponseDTO;
import cl.smartlogix.pedidos.entity.Pedido;
import cl.smartlogix.pedidos.mapper.PedidoMapper;
import cl.smartlogix.pedidos.service.PedidoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;
    private final PedidoMapper pedidoMapper;

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
    public List<PedidoResponseDTO> listarPedidos(@AuthenticationPrincipal Jwt jwt) {
        String usuarioId = jwt.getClaimAsString("sub");
        boolean isGestor = esGestor(jwt);

        List<Pedido> pedidos;
        if (isGestor) {
            pedidos = pedidoService.listarPedidos();
        } else {
            pedidos = pedidoService.listarPedidosPorUsuario(usuarioId);
        }
        return pedidos.stream()
                .map(pedidoMapper::toResponseDTO)
                .collect(Collectors.toList());
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