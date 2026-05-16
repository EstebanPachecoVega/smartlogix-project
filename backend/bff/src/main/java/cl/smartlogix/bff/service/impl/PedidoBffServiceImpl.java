package cl.smartlogix.bff.service.impl;

import cl.smartlogix.bff.client.PedidosClient;
import cl.smartlogix.bff.dto.request.CrearPedidoRequestDTO;
import cl.smartlogix.bff.dto.response.PedidoResponseDTO;
import cl.smartlogix.bff.service.PedidoBffService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PedidoBffServiceImpl implements PedidoBffService {
    private final PedidosClient pedidosClient;

    @Override
    public Mono<PedidoResponseDTO> crearPedido(CrearPedidoRequestDTO request) {
        return pedidosClient.crearPedido(request);
    }

    @Override
    public Mono<List<PedidoResponseDTO>> listarPedidos() {
        return pedidosClient.listarPedidos();
    }

    @Override
    public Mono<PedidoResponseDTO> obtenerPedidoPorId(Long id) {
        return pedidosClient.obtenerPedido(id);
    }
}