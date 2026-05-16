package cl.smartlogix.bff.service.impl;

import cl.smartlogix.bff.client.EnviosClient;
import cl.smartlogix.bff.dto.response.EnvioResponseDTO;
import cl.smartlogix.bff.service.EnvioBffService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EnvioBffServiceImpl implements EnvioBffService {
    private final EnviosClient enviosClient;

    @Override
    public Mono<List<EnvioResponseDTO>> listarEnvios() {
        return enviosClient.listarEnvios();
    }

    @Override
    public Mono<EnvioResponseDTO> obtenerEnvio(Long id) {
        return enviosClient.obtenerEnvio(id);
    }
}