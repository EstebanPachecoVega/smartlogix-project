package cl.smartlogix.bff.controller;

import cl.smartlogix.bff.client.GatewayClient;
import cl.smartlogix.bff.dto.response.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/bff/estadisticas")
@RequiredArgsConstructor
public class EstadisticasController {

    private final GatewayClient gatewayClient;

    @GetMapping("/ventas-plataforma")
    public Mono<List<VentasPlataformaResponseDTO>> getVentasPlataforma(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = true) String authorization) {
        String jwt = extractJwt(authorization);
        return gatewayClient.getVentasPlataforma(jwt, MDC.get("correlationId"));
    }

    @GetMapping("/comparacion-anual")
    public Mono<List<ComparacionAnualResponseDTO>> getComparacionAnual(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = true) String authorization) {
        String jwt = extractJwt(authorization);
        return gatewayClient.getComparacionAnual(jwt, MDC.get("correlationId"));
    }

    @GetMapping("/ventas-por-categoria")
    public Mono<List<VentaPorCategoriaResponseDTO>> getVentasPorCategoria(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = true) String authorization) {
        String jwt = extractJwt(authorization);
        String correlationId = MDC.get("correlationId");

        Mono<List<VentaPorProductoResponseDTO>> ventasMono = gatewayClient.getVentasPorProducto(jwt, correlationId);
        Mono<Map<Long, String>> categoriasMono = gatewayClient.getMapaCategorias(jwt, correlationId)
                .map(lista -> lista.stream()
                        .collect(Collectors.toMap(MapaCategoriaResponseDTO::getProductoId,
                                MapaCategoriaResponseDTO::getCategoriaNombre)));

        return ventasMono.zipWith(categoriasMono, (ventas, categorias) ->
                ventas.stream()
                        .map(v -> new VentaPorCategoriaResponseDTO(
                                categorias.getOrDefault(v.getProductoId(), "Sin categoría"),
                                v.getTotalVentas()))
                        .collect(Collectors.groupingBy(VentaPorCategoriaResponseDTO::getCategoria,
                                Collectors.summingLong(VentaPorCategoriaResponseDTO::getTotalVentas)))
                        .entrySet().stream()
                        .map(e -> new VentaPorCategoriaResponseDTO(e.getKey(), e.getValue()))
                        .sorted((a, b) -> Long.compare(b.getTotalVentas(), a.getTotalVentas()))
                        .collect(Collectors.toList())
        );
    }

    private String extractJwt(String authorization) {
        return authorization.substring(7);
    }
}
