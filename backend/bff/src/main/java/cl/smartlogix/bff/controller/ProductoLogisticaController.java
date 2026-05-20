package cl.smartlogix.bff.controller;

import cl.smartlogix.bff.client.GatewayClient;
import cl.smartlogix.bff.dto.request.ProductoRequestDTO;
import cl.smartlogix.bff.dto.response.ProductoResponseDTO;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import java.util.List;

@RestController
@RequestMapping("/bff/logistica/productos")
@RequiredArgsConstructor
public class ProductoLogisticaController {
    private final GatewayClient gatewayClient;

    @GetMapping
    public Mono<List<ProductoResponseDTO>> listar(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String auth) {
        String jwt = (auth != null && auth.startsWith("Bearer ")) ? auth.substring(7) : "dev-token";
        return gatewayClient.getProductos(jwt, MDC.get("correlationId"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ProductoResponseDTO> crear(@RequestBody ProductoRequestDTO request,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String auth) {
        String jwt = (auth != null && auth.startsWith("Bearer ")) ? auth.substring(7) : "dev-token";
        return gatewayClient.crearProducto(request, jwt, MDC.get("correlationId"));
    }

    @PutMapping("/{id}")
    public Mono<ProductoResponseDTO> actualizar(@PathVariable Long id, @RequestBody ProductoRequestDTO request,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String auth) {
        String jwt = (auth != null && auth.startsWith("Bearer ")) ? auth.substring(7) : "dev-token";
        return gatewayClient.actualizarProducto(id, request, jwt, MDC.get("correlationId"));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> eliminar(@PathVariable Long id,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String auth) {
        String jwt = (auth != null && auth.startsWith("Bearer ")) ? auth.substring(7) : "dev-token";
        return gatewayClient.eliminarProducto(id, jwt, MDC.get("correlationId"));
    }
}