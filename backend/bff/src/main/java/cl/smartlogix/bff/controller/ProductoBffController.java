package cl.smartlogix.bff.controller;

import cl.smartlogix.bff.client.GatewayClient;
import cl.smartlogix.bff.dto.response.ProductoResponseDTO;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import java.util.List;

@RestController
@RequestMapping("/bff/productos")
@RequiredArgsConstructor
public class ProductoBffController {
    private final GatewayClient gatewayClient;

    @GetMapping
    public Mono<List<ProductoResponseDTO>> listarProductos(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String auth) {
        String jwt = (auth != null && auth.startsWith("Bearer ")) ? auth.substring(7) : "dev-token";
        return gatewayClient.getProductos(jwt, MDC.get("correlationId"));
    }
}