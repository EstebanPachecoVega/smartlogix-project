package cl.smartlogix.bff.service;

import cl.smartlogix.bff.dto.response.EnvioResponseDTO;
import reactor.core.publisher.Mono;

import java.util.List;

public interface EnvioBffService {
    Mono<List<EnvioResponseDTO>> listarEnvios();

    Mono<EnvioResponseDTO> obtenerEnvio(Long id);

    Mono<EnvioResponseDTO> obtenerEnvioPorPedidoId(Long pedidoId);
}