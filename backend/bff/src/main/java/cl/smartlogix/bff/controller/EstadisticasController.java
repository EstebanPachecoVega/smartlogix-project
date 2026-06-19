package cl.smartlogix.bff.controller;

import cl.smartlogix.bff.client.GatewayClient;
import cl.smartlogix.bff.dto.response.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.*;
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

        // 1. Obtener todas las categorías
        Mono<List<String>> todasLasCategoriasMono = gatewayClient.listarCategorias(jwt, correlationId)
                .map(lista -> lista.stream()
                        .map(CategoriaResponseDTO::getNombre)
                        .sorted()
                        .collect(Collectors.toList()));

        // 2. Obtener cantidad por producto
        Mono<List<VentaPorProductoCantidadDTO>> cantidadMono = gatewayClient.getCantidadPorProducto(jwt, correlationId);

        // 3. Obtener mapa producto → categoría
        Mono<Map<Long, String>> mapaCategoriasMono = gatewayClient.getMapaCategorias(jwt, correlationId)
                .map(lista -> lista.stream()
                        .collect(Collectors.toMap(MapaCategoriaResponseDTO::getProductoId,
                                MapaCategoriaResponseDTO::getCategoriaNombre)));

        // Combinar: merge cantidad × mapa-categorias → agrupar por categoría → rellenar 0s
        return Mono.zip(todasLasCategoriasMono, cantidadMono, mapaCategoriasMono)
                .map(tuple -> {
                    List<String> todasCategorias = tuple.getT1();
                    List<VentaPorProductoCantidadDTO> cantidades = tuple.getT2();
                    Map<Long, String> mapaCat = tuple.getT3();

                    Map<String, Long> porCategoria = cantidades.stream()
                            .collect(Collectors.groupingBy(
                                    c -> mapaCat.getOrDefault(c.getProductoId(), "Sin categoría"),
                                    Collectors.summingLong(VentaPorProductoCantidadDTO::getCantidad)));

                    return todasCategorias.stream()
                            .map(cat -> new VentaPorCategoriaResponseDTO(cat,
                                    porCategoria.getOrDefault(cat, 0L)))
                            .collect(Collectors.toList());
                });
    }

    private String extractJwt(String authorization) {
        return authorization.substring(7);
    }
}
