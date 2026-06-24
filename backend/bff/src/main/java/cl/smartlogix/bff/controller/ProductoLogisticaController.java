package cl.smartlogix.bff.controller;

import cl.smartlogix.bff.client.GatewayClient;
import cl.smartlogix.bff.dto.request.ProductoRequestDTO;
import cl.smartlogix.bff.dto.response.ProductoResponseDTO;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/bff/logistica/productos")
@RequiredArgsConstructor
public class ProductoLogisticaController {
    private final GatewayClient gatewayClient;

    @GetMapping
    public Mono<List<ProductoResponseDTO>> listar(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        validateBearerToken(authorization);
        String jwt = extractJwt(authorization);
        return gatewayClient.getProductos(jwt, MDC.get("correlationId"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ProductoResponseDTO> crear(@RequestBody ProductoRequestDTO request,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        validateBearerToken(authorization);
        String jwt = extractJwt(authorization);
        return gatewayClient.crearProducto(request, jwt, MDC.get("correlationId"));
    }

    @GetMapping("/{id}")
    public Mono<ProductoResponseDTO> obtener(@PathVariable Long id,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        validateBearerToken(authorization);
        String jwt = extractJwt(authorization);
        return gatewayClient.obtenerProducto(id, jwt, MDC.get("correlationId"));
    }

    @PutMapping("/{id}")
    public Mono<ProductoResponseDTO> actualizar(@PathVariable Long id, @RequestBody ProductoRequestDTO request,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        validateBearerToken(authorization);
        String jwt = extractJwt(authorization);
        return gatewayClient.actualizarProducto(id, request, jwt, MDC.get("correlationId"));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> eliminar(@PathVariable Long id,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        validateBearerToken(authorization);
        String jwt = extractJwt(authorization);
        return gatewayClient.eliminarProducto(id, jwt, MDC.get("correlationId"));
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