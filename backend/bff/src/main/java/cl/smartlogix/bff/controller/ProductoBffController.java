package cl.smartlogix.bff.controller;

import cl.smartlogix.bff.client.GatewayClient;
import cl.smartlogix.bff.dto.response.PagedResponse;
import cl.smartlogix.bff.dto.response.ProductoResponseDTO;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/bff/productos")
@RequiredArgsConstructor
public class ProductoBffController {
    private final GatewayClient gatewayClient;

    @GetMapping
    public Mono<PagedResponse<ProductoResponseDTO>> listar(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        validateBearerToken(authorization);
        String jwt = extractJwt(authorization);
        return gatewayClient.getProductos(jwt, MDC.get("correlationId"), page, size);
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