package cl.smartlogix.envios.service;

import cl.smartlogix.envios.dto.response.EnvioResponseDTO;
import cl.smartlogix.envios.entity.EstadoEnvio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface EnvioService {
    EnvioResponseDTO actualizarEstado(Long id, EstadoEnvio nuevoEstado);

    List<EnvioResponseDTO> listarTodos();

    Page<EnvioResponseDTO> listarTodos(Pageable pageable);

    EnvioResponseDTO obtenerPorId(Long id);

    EnvioResponseDTO obtenerPorPedidoId(Long pedidoId);

    EnvioResponseDTO obtenerPorTracking(String numeroTracking);

    List<EnvioResponseDTO> listarPorEstado(EstadoEnvio estado);

    List<EnvioResponseDTO> listarEnviosConProblemas();
}