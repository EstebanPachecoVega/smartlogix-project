package cl.smartlogix.envios.service.impl;

import cl.smartlogix.envios.dto.response.EnvioResponseDTO;
import cl.smartlogix.envios.entity.Envio;
import cl.smartlogix.envios.entity.EstadoEnvio;
import cl.smartlogix.envios.exception.ResourceNotFoundException;
import cl.smartlogix.envios.mapper.EnvioMapper;
import cl.smartlogix.envios.repository.EnvioRepository;
import cl.smartlogix.envios.service.EnvioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnvioServiceImpl implements EnvioService {

        private final EnvioRepository envioRepository;
        private final EnvioMapper envioMapper;
        private final RabbitTemplate rabbitTemplate;

        // Constantes para el Exchange de Logística
        private static final String ENVIO_EXCHANGE = "envio.exchange";
        private static final String ROUTING_KEY_ENVIO_ACTUALIZADO = "envio.actualizado";

        @Override
        @Transactional
        public EnvioResponseDTO actualizarEstado(Long id, EstadoEnvio nuevoEstado) {
                Envio envio = envioRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Envío no encontrado con ID: " + id));

                // 1. REGLA DE IDEMPOTENCIA: Si el estado actual es igual al nuevo, se procesa
                // exitosamente sin recalcular
                if (envio.getEstadoEnvio() == nuevoEstado) {
                        log.info("El envío ID {} ya se encuentra en estado [{}]. Operación omitida por idempotencia.",
                                        id, nuevoEstado);
                        return envioMapper.toResponseDTO(envio);
                }

                // 2. MÁQUINA DE ESTADOS: Bloquear retrocesos usando comparación de índices
                if (nuevoEstado.ordinal() < envio.getEstadoEnvio().ordinal()) {
                        throw new IllegalArgumentException("Retroceso no permitido. No se puede pasar de '"
                                        + envio.getEstadoEnvio().getTexto() + "' a '" + nuevoEstado.getTexto() + "'");
                }

                log.info("Transición aprobada para Envío ID {}: {} a {}", id, envio.getEstadoEnvio(), nuevoEstado);

                // Persistir cambio
                envio.setEstadoEnvio(nuevoEstado);
                envioRepository.save(envio);

                // 3. PUBLICACIÓN DEL EVENTO SAGA: Informar la novedad de transporte a
                // ms-pedidos
                try {
                        Map<String, Object> evento = new HashMap<>();
                        evento.put("pedidoId", envio.getPedidoId());
                        evento.put("envioId", envio.getId());
                        evento.put("estadoEnvio", nuevoEstado.name()); // "EN_TRANSITO", "EN_REPARTO", etc.

                        rabbitTemplate.convertAndSend(ENVIO_EXCHANGE, ROUTING_KEY_ENVIO_ACTUALIZADO, evento);
                        log.info("[EVENTO PUBLICADO] Novedad de envío enviada a RabbitMQ para Pedido ID: {}",
                                        envio.getPedidoId());
                } catch (Exception e) {
                        log.error("ERROR CRÍTICO al enviar evento de actualización a RabbitMQ: {}", e.getMessage());
                }

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