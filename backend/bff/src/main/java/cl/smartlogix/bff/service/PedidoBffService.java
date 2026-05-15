package cl.smartlogix.bff.service;

import cl.smartlogix.bff.dto.request.CrearPedidoRequestDTO;
import cl.smartlogix.bff.dto.response.PedidoResponseDTO;
import reactor.core.publisher.Mono;

public interface PedidoBffService {
    Mono<PedidoResponseDTO> crearPedido(CrearPedidoRequestDTO request);
    Mono<PedidoResponseDTO> obtenerPedidoPorId(Long id);
}