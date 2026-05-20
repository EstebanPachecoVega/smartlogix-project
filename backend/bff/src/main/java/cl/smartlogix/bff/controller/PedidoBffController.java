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
    public Mono<PedidoResponseDTO> crear(@Valid @RequestBody CrearPedidoRequestDTO request,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String auth,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        String jwt = (auth != null && auth.startsWith("Bearer ")) ? auth.substring(7) : "dev-token";
        return gatewayClient.crearPedido(request, jwt, MDC.get("correlationId"));
    }

    @GetMapping
    public Mono<List<PedidoResponseDTO>> listar(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String auth) {
        String jwt = (auth != null && auth.startsWith("Bearer ")) ? auth.substring(7) : "dev-token";
        return gatewayClient.listarPedidos(jwt, MDC.get("correlationId"));
    }

    @GetMapping("/{id}")
    public Mono<PedidoResponseDTO> obtener(@PathVariable Long id,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String auth) {
        String jwt = (auth != null && auth.startsWith("Bearer ")) ? auth.substring(7) : "dev-token";
        return gatewayClient.obtenerPedido(id, jwt, MDC.get("correlationId"));
    }
}