package cl.smartlogix.envios.controller;

import cl.smartlogix.envios.dto.response.EnvioResponseDTO;
import cl.smartlogix.envios.entity.EstadoEnvio;
import cl.smartlogix.envios.service.EnvioService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/envios")
@RequiredArgsConstructor
public class EnvioController {

    private final EnvioService envioService;

    @GetMapping
    public ResponseEntity<Page<EnvioResponseDTO>> listarEnvios(
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(required = false) String estadoEnvio) {
        if (estadoEnvio != null && estadoEnvio.contains(",")) {
            List<EstadoEnvio> estados = Arrays.stream(estadoEnvio.split(","))
                    .map(EstadoEnvio::valueOf)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(envioService.listarPorEstados(estados, pageable));
        } else if (estadoEnvio != null) {
            return ResponseEntity.ok(envioService.listarTodos(pageable, EstadoEnvio.valueOf(estadoEnvio)));
        }
        return ResponseEntity.ok(envioService.listarTodos(pageable));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<EnvioResponseDTO> actualizarEstadoEnvio(
            @PathVariable Long id,
            @RequestParam EstadoEnvio nuevoEstado) {
        EnvioResponseDTO envioActualizado = envioService.actualizarEstado(id, nuevoEstado);
        return ResponseEntity.ok(envioActualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarEnvio(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Jwt jwt) || !esGestor(jwt)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Solo gestores pueden eliminar envíos");
        }
        envioService.deleteEnvio(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<EnvioResponseDTO> obtenerEnvio(@PathVariable Long id) {
        return ResponseEntity.ok(envioService.obtenerPorId(id));
    }

    @GetMapping("/pedido/{pedidoId}")
    public ResponseEntity<EnvioResponseDTO> obtenerEnvioPorPedidoId(@PathVariable Long pedidoId) {
        return ResponseEntity.ok(envioService.obtenerPorPedidoId(pedidoId));
    }

    @GetMapping("/tracking/{numeroTracking}")
    public ResponseEntity<EnvioResponseDTO> obtenerEnvioPorTracking(@PathVariable String numeroTracking) {
        return ResponseEntity.ok(envioService.obtenerPorTracking(numeroTracking));
    }

    @GetMapping("/problemas")
    public ResponseEntity<List<EnvioResponseDTO>> listarEnviosConProblemas() {
        return ResponseEntity.ok(envioService.listarEnviosConProblemas());
    }

    @SuppressWarnings("unchecked")
    private boolean esGestor(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess == null)
            return false;
        List<String> roles = (List<String>) realmAccess.get("roles");
        return roles != null && roles.contains("gestor");
    }
}