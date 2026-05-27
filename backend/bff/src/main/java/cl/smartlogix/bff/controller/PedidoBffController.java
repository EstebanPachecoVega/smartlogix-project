package cl.smartlogix.bff.controller;

import cl.smartlogix.bff.client.GatewayClient;
import cl.smartlogix.bff.dto.request.CrearPedidoRequestDTO;
import cl.smartlogix.bff.dto.response.PedidoResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/bff/pedidos")
@RequiredArgsConstructor
public class PedidoBffController {
    private final GatewayClient gatewayClient;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<PedidoResponseDTO> crear(@Valid @RequestBody CrearPedidoRequestDTO request,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = true) String authorization,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        validateBearerToken(authorization);
        String jwt = extractJwt(authorization);
        // Nota: idempotencyKey se reenvía en el header por el GatewayClient si se
        // implementa,
        // por ahora no se usa en el cliente.
        return gatewayClient.crearPedido(request, jwt, MDC.get("correlationId"));
    }

    @GetMapping
    public Mono<List<PedidoResponseDTO>> listar(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = true) String authorization) {
        validateBearerToken(authorization);
        String jwt = extractJwt(authorization);
        return gatewayClient.listarPedidos(jwt, MDC.get("correlationId"));
    }

    @GetMapping("/{id}")
    public Mono<PedidoResponseDTO> obtener(@PathVariable Long id,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = true) String authorization) {
        validateBearerToken(authorization);
        String jwt = extractJwt(authorization);
        return gatewayClient.obtenerPedido(id, jwt, MDC.get("correlationId"));
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