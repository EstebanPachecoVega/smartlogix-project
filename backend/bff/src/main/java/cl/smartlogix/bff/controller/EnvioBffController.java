package cl.smartlogix.bff.controller;

import cl.smartlogix.bff.dto.response.EnvioResponseDTO;
import cl.smartlogix.bff.service.EnvioBffService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/bff/envios")
@RequiredArgsConstructor
public class EnvioBffController {
    private final EnvioBffService envioBffService;

    @GetMapping("/{id}")
    public Mono<EnvioResponseDTO> obtenerEnvio(@PathVariable Long id) {
        return envioBffService.obtenerEnvio(id);
    }
}