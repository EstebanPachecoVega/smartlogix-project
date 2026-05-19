package cl.smartlogix.bff.controller;

import cl.smartlogix.bff.client.GatewayClient;
import cl.smartlogix.bff.dto.response.EnvioResponseDTO;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import java.util.List;

@RestController
@RequestMapping("/bff/envios")
@RequiredArgsConstructor
public class EnvioBffController {
    private final GatewayClient gatewayClient;

    @GetMapping("/{id}")
    public Mono<EnvioResponseDTO> obtenerEnvio(@PathVariable Long id,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String auth) {
        String jwt = (auth != null && auth.startsWith("Bearer ")) ? auth.substring(7) : "dev-token";
        return gatewayClient.obtenerEnvioPorId(id, jwt, MDC.get("correlationId"));
    }

    @GetMapping
    public Mono<List<EnvioResponseDTO>> listarEnvios(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String auth) {
        String jwt = (auth != null && auth.startsWith("Bearer ")) ? auth.substring(7) : "dev-token";
        return gatewayClient.listarEnvios(jwt, MDC.get("correlationId"));
    }

    @PatchMapping("/{id}/estado")
    public Mono<EnvioResponseDTO> actualizarEstado(@PathVariable Long id, @RequestParam String nuevoEstado,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String auth) {
        String jwt = (auth != null && auth.startsWith("Bearer ")) ? auth.substring(7) : "dev-token";
        return gatewayClient.actualizarEstadoEnvio(id, nuevoEstado, jwt, MDC.get("correlationId"));
    }

    @GetMapping("/tracking/{tracking}")
    public Mono<EnvioResponseDTO> obtenerPorTracking(@PathVariable String tracking,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String auth) {
        String jwt = (auth != null && auth.startsWith("Bearer ")) ? auth.substring(7) : "dev-token";
        return gatewayClient.obtenerEnvioPorTracking(tracking, jwt, MDC.get("correlationId"));
    }

    @GetMapping("/problemas")
    public Mono<List<EnvioResponseDTO>> listarProblemas(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String auth) {
        return listarEnvios(auth);
    }
}