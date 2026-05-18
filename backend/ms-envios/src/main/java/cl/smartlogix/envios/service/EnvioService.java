package cl.smartlogix.envios.service;

import cl.smartlogix.envios.dto.response.EnvioResponseDTO;
import cl.smartlogix.envios.entity.EstadoEnvio;
import java.util.List;

public interface EnvioService {
    EnvioResponseDTO actualizarEstado(Long id, EstadoEnvio nuevoEstado);

    List<EnvioResponseDTO> listarTodos();

    EnvioResponseDTO obtenerPorId(Long id);

    EnvioResponseDTO obtenerPorPedidoId(Long pedidoId);

    EnvioResponseDTO obtenerPorTracking(String numeroTracking);

    List<EnvioResponseDTO> listarPorEstado(EstadoEnvio estado);

    List<EnvioResponseDTO> listarEnviosConProblemas(); // Retrasados, fallidos, devueltos
}