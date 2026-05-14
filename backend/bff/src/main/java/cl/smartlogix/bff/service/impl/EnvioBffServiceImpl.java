package cl.smartlogix.bff.service.impl;

import cl.smartlogix.bff.client.EnviosClientService;
import cl.smartlogix.bff.dto.response.EnvioResponseDTO;
import cl.smartlogix.bff.service.EnvioBffService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class EnvioBffServiceImpl implements EnvioBffService {
    private final EnviosClientService enviosWebClient;

    @Override
    public Mono<EnvioResponseDTO> obtenerEnvio(Long id) {
        return enviosWebClient.obtenerEnvio(id);
    }
}