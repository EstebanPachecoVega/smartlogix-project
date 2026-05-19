package cl.smartlogix.envios.service.impl;

import cl.smartlogix.envios.dto.event.EnvioActualizadoEventDTO;
import cl.smartlogix.envios.dto.response.EnvioResponseDTO;
import cl.smartlogix.envios.entity.Envio;
import cl.smartlogix.envios.entity.EstadoEnvio;
import cl.smartlogix.envios.exception.ResourceNotFoundException;
import cl.smartlogix.envios.mapper.EnvioMapper;
import cl.smartlogix.envios.publisher.EnvioEventPublisher;
import cl.smartlogix.envios.repository.EnvioRepository;
import cl.smartlogix.envios.service.EnvioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnvioServiceImpl implements EnvioService {

        private final EnvioRepository envioRepository;
        private final EnvioMapper envioMapper;
        private final EnvioEventPublisher eventPublisher;

        @Override
        @Transactional
        public EnvioResponseDTO actualizarEstado(Long id, EstadoEnvio nuevoEstado) {
                Envio envio = envioRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Envío no encontrado con ID: " + id));

                if (envio.getEstadoEnvio() == nuevoEstado) {
                        log.info("El envío ID {} ya se encuentra en estado [{}]. Operación omitida por idempotencia.",
                                        id, nuevoEstado);
                        return envioMapper.toResponseDTO(envio);
                }

                if (nuevoEstado.ordinal() < envio.getEstadoEnvio().ordinal()) {
                        throw new IllegalArgumentException("Retroceso no permitido. No se puede pasar de '"
                                        + envio.getEstadoEnvio().getTexto() + "' a '" + nuevoEstado.getTexto() + "'");
                }

                log.info("Transición aprobada para Envío ID {}: {} a {}", id, envio.getEstadoEnvio(), nuevoEstado);
                envio.setEstadoEnvio(nuevoEstado);
                envioRepository.save(envio);

                // Publicar evento tipado
                EnvioActualizadoEventDTO evento = EnvioActualizadoEventDTO.builder()
                                .pedidoId(envio.getPedidoId())
                                .envioId(envio.getId())
                                .numeroTracking(envio.getNumeroTracking())
                                .estadoEnvio(nuevoEstado)
                                .build();
                eventPublisher.publicarEnvioActualizado(evento);

                return envioMapper.toResponseDTO(envio);
        }

        @Override
        @Transactional(readOnly = true)
        public List<EnvioResponseDTO> listarTodos() {
                return envioRepository.findAll().stream()
                                .map(envioMapper::toResponseDTO)
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional(readOnly = true)
        public EnvioResponseDTO obtenerPorId(Long id) {
                Envio envio = envioRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Envío no encontrado con ID: " + id));
                return envioMapper.toResponseDTO(envio);
        }

        @Override
        @Transactional(readOnly = true)
        public EnvioResponseDTO obtenerPorPedidoId(Long pedidoId) {
                Envio envio = envioRepository.findByPedidoId(pedidoId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "No existe un envío para el pedido ID: " + pedidoId));
                return envioMapper.toResponseDTO(envio);
        }

        @Override
        @Transactional(readOnly = true)
        public EnvioResponseDTO obtenerPorTracking(String numeroTracking) {
                Envio envio = envioRepository.findByNumeroTracking(numeroTracking)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "No se encontró ningún despacho con tracking: " + numeroTracking));
                return envioMapper.toResponseDTO(envio);
        }

        @Override
        @Transactional(readOnly = true)
        public List<EnvioResponseDTO> listarPorEstado(EstadoEnvio estado) {
                return envioRepository.findByEstadoEnvio(estado).stream()
                                .map(envioMapper::toResponseDTO)
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional(readOnly = true)
        public List<EnvioResponseDTO> listarEnviosConProblemas() {
                List<EstadoEnvio> estadosProblema = Arrays.asList(
                                EstadoEnvio.INTENTO_FALLIDO,
                                EstadoEnvio.RETRASADO,
                                EstadoEnvio.DEVUELTO);
                return envioRepository.findAll().stream()
                                .filter(e -> estadosProblema.contains(e.getEstadoEnvio()))
                                .map(envioMapper::toResponseDTO)
                                .collect(Collectors.toList());
        }
}