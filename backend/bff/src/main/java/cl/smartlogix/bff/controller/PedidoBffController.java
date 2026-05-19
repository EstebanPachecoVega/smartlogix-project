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
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/bff/pedidos")
@RequiredArgsConstructor
public class PedidoBffController {

    private final GatewayClient gatewayClient;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<PedidoResponseDTO> crearPedido(
            @Valid @RequestBody CrearPedidoRequestDTO request,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        String jwt = "dev-token"; // Token por defecto para desarrollo
        if (authorization != null && authorization.startsWith("Bearer ")) {
            jwt = authorization.substring(7);
        }
        String correlationId = MDC.get("correlationId");
        return gatewayClient.crearPedido(request, jwt, correlationId);
    }

    @GetMapping
    public Mono<List<PedidoResponseDTO>> listarPedidos(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        String jwt = "dev-token";
        if (authorization != null && authorization.startsWith("Bearer ")) {
            jwt = authorization.substring(7);
        }
        String correlationId = MDC.get("correlationId");
        return gatewayClient.listarPedidos(jwt, correlationId);
    }

    @GetMapping("/{id}")
    public Mono<PedidoResponseDTO> obtenerPedido(
            @PathVariable Long id,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        String jwt = "dev-token";
        if (authorization != null && authorization.startsWith("Bearer ")) {
            jwt = authorization.substring(7);
        }
        String correlationId = MDC.get("correlationId");
        return gatewayClient.obtenerPedido(id, jwt, correlationId);
    }
}