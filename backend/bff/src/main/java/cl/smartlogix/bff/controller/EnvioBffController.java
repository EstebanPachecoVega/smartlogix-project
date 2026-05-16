package cl.smartlogix.bff.controller;

import cl.smartlogix.bff.dto.response.EnvioResponseDTO;
import cl.smartlogix.bff.service.EnvioBffService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/bff/envios")
@RequiredArgsConstructor
public class EnvioBffController {
    private final EnvioBffService envioBffService;

    @GetMapping
    public Mono<List<EnvioResponseDTO>> listarEnvios() {
        return envioBffService.listarEnvios();
    }

    @GetMapping("/{id}")
    public Mono<EnvioResponseDTO> obtenerEnvio(@PathVariable Long id) {
        return envioBffService.obtenerEnvio(id);
    }

    @GetMapping("/pedido/{pedidoId}")
    public Mono<EnvioResponseDTO> obtenerEnvioPorPedidoId(@PathVariable Long pedidoId) {
        return envioBffService.obtenerEnvioPorPedidoId(pedidoId);
    }
}