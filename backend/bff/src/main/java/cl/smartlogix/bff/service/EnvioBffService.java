package cl.smartlogix.bff.service;

import cl.smartlogix.bff.dto.response.EnvioResponseDTO;
import reactor.core.publisher.Mono;

public interface EnvioBffService {
    Mono<EnvioResponseDTO> obtenerEnvio(Long id);
}