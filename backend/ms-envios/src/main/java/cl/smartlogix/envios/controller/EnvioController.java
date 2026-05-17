package cl.smartlogix.envios.controller;

import cl.smartlogix.envios.dto.response.EnvioResponseDTO;
import cl.smartlogix.envios.entity.Envio;
import cl.smartlogix.envios.exception.ResourceNotFoundException;
import cl.smartlogix.envios.mapper.EnvioMapper;
import cl.smartlogix.envios.repository.EnvioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/envios")
@RequiredArgsConstructor
public class EnvioController {

    private final EnvioRepository envioRepository;
    private final EnvioMapper envioMapper;

    @GetMapping
    public List<EnvioResponseDTO> listarEnvios() {
        return envioRepository.findAll()
                .stream()
                .map(envioMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public EnvioResponseDTO obtenerEnvio(@PathVariable Long id) {
        Envio envio = envioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Envío no encontrado con ID: " + id));
        return envioMapper.toResponseDTO(envio);
    }

    @GetMapping("/pedido/{pedidoId}")
    public EnvioResponseDTO obtenerEnvioPorPedidoId(@PathVariable Long pedidoId) {
        Envio envio = envioRepository.findByPedidoId(pedidoId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe un proceso de envío para el pedido ID: " + pedidoId));
        return envioMapper.toResponseDTO(envio);
    }

    // 🚀 Endpoint robusto e indispensable para tracking logístico
    @GetMapping("/tracking/{numeroTracking}")
    public EnvioResponseDTO obtenerEnvioPorTracking(@PathVariable String numeroTracking) {
        Envio envio = envioRepository.findByNumeroTracking(numeroTracking)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró ningún despacho asociado al tracking: " + numeroTracking));
        return envioMapper.toResponseDTO(envio);
    }
}