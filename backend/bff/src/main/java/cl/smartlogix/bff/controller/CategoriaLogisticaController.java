package cl.smartlogix.bff.controller;

import cl.smartlogix.bff.client.GatewayClient;
import cl.smartlogix.bff.dto.request.CategoriaRequestDTO;
import cl.smartlogix.bff.dto.request.ReordenarCategoriaDTO;
import cl.smartlogix.bff.dto.response.CategoriaResponseDTO;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/bff/logistica/categorias")
@RequiredArgsConstructor
public class CategoriaLogisticaController {
    private final GatewayClient gatewayClient;

    @GetMapping
    public Mono<List<CategoriaResponseDTO>> listar(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        validateBearerToken(authorization);
        String jwt = extractJwt(authorization);
        return gatewayClient.listarCategorias(jwt, MDC.get("correlationId"));
    }

    @GetMapping("/{id}")
    public Mono<CategoriaResponseDTO> obtener(@PathVariable Long id,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        validateBearerToken(authorization);
        String jwt = extractJwt(authorization);
        return gatewayClient.obtenerCategoria(id, jwt, MDC.get("correlationId"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<CategoriaResponseDTO> crear(@RequestBody CategoriaRequestDTO request,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        validateBearerToken(authorization);
        String jwt = extractJwt(authorization);
        return gatewayClient.crearCategoria(request, jwt, MDC.get("correlationId"));
    }

    @PutMapping("/{id}")
    public Mono<CategoriaResponseDTO> actualizar(@PathVariable Long id, @RequestBody CategoriaRequestDTO request,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        validateBearerToken(authorization);
        String jwt = extractJwt(authorization);
        return gatewayClient.actualizarCategoria(id, request, jwt, MDC.get("correlationId"));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> eliminar(@PathVariable Long id,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        validateBearerToken(authorization);
        String jwt = extractJwt(authorization);
        return gatewayClient.eliminarCategoria(id, jwt, MDC.get("correlationId"));
    }

    @PatchMapping("/reordenar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> reordenar(@RequestBody List<ReordenarCategoriaDTO> ordenes,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        validateBearerToken(authorization);
        String jwt = extractJwt(authorization);
        return gatewayClient.reordenarCategorias(ordenes, jwt, MDC.get("correlationId"));
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