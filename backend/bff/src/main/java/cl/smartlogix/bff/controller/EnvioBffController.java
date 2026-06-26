package cl.smartlogix.bff.controller;

import cl.smartlogix.bff.client.GatewayClient;
import cl.smartlogix.bff.dto.response.EnvioResponseDTO;
import cl.smartlogix.bff.dto.response.PagedResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/bff/envios")
@RequiredArgsConstructor
public class EnvioBffController {
    private final GatewayClient gatewayClient;

    @GetMapping
    public Mono<PagedResponse<EnvioResponseDTO>> listar(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        validateBearerToken(authorization);
        String jwt = extractJwt(authorization);
        return gatewayClient.listarEnvios(jwt, MDC.get("correlationId"), page, size);
    }

    @GetMapping("/{id}")
    public Mono<EnvioResponseDTO> obtener(@PathVariable Long id,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        validateBearerToken(authorization);
        String jwt = extractJwt(authorization);
        return gatewayClient.obtenerEnvio(id, jwt, MDC.get("correlationId"));
    }

    @GetMapping("/pedido/{pedidoId}")
    public Mono<EnvioResponseDTO> obtenerPorPedidoId(@PathVariable Long pedidoId,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        validateBearerToken(authorization);
        String jwt = extractJwt(authorization);
        return gatewayClient.obtenerEnvioPorPedidoId(pedidoId, jwt, MDC.get("correlationId"));
    }

    @PatchMapping("/{id}/estado")
    public Mono<EnvioResponseDTO> actualizarEstado(@PathVariable Long id, @RequestParam String nuevoEstado,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        validateBearerToken(authorization);
        String jwt = extractJwt(authorization);
        return gatewayClient.actualizarEstadoEnvio(id, nuevoEstado, jwt, MDC.get("correlationId"));
    }

    @GetMapping("/tracking/{tracking}")
    public Mono<EnvioResponseDTO> obtenerPorTracking(@PathVariable String tracking,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        validateBearerToken(authorization);
        String jwt = extractJwt(authorization);
        return gatewayClient.obtenerEnvioPorTracking(tracking, jwt, MDC.get("correlationId"));
    }

    private void validateBearerToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token de autorización requerido");
        }
    }

    private String extractJwt(String authorization) {
        return authorization.substring(7);
    }
}