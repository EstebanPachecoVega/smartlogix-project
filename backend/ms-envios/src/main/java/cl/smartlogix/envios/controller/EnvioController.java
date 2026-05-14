package cl.smartlogix.envios.controller;

import cl.smartlogix.envios.dto.response.EnvioResponseDTO;
import cl.smartlogix.envios.exception.ResourceNotFoundException;
import cl.smartlogix.envios.mapper.EnvioMapper;
import cl.smartlogix.envios.repository.EnvioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/envios")
@RequiredArgsConstructor
public class EnvioController {
    private final EnvioRepository envioRepository;
    private final EnvioMapper envioMapper;

    @GetMapping("/{id}")
    public EnvioResponseDTO obtenerEnvio(@PathVariable Long id) {
        var envio = envioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Envío no encontrado con id " + id));
        return envioMapper.toResponseDTO(envio);
    }
}