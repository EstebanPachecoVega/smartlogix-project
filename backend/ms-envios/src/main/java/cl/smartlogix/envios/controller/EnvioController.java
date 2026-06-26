package cl.smartlogix.envios.controller;

import cl.smartlogix.envios.dto.response.EnvioResponseDTO;
import cl.smartlogix.envios.entity.EstadoEnvio;
import cl.smartlogix.envios.service.EnvioService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/envios")
@RequiredArgsConstructor
public class EnvioController {

    private final EnvioService envioService;

    @GetMapping
    public ResponseEntity<Page<EnvioResponseDTO>> listarEnvios(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(envioService.listarTodos(pageable));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<EnvioResponseDTO> actualizarEstadoEnvio(
            @PathVariable Long id,
            @RequestParam EstadoEnvio nuevoEstado) {
        EnvioResponseDTO envioActualizado = envioService.actualizarEstado(id, nuevoEstado);
        return ResponseEntity.ok(envioActualizado);
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
}