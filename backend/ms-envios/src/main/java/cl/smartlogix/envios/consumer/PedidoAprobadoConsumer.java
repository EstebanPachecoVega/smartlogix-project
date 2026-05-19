package cl.smartlogix.envios.consumer;

import cl.smartlogix.envios.config.RabbitMQConfig;
import cl.smartlogix.envios.dto.event.PedidoAprobadoEventDTO;
import cl.smartlogix.envios.entity.Envio;
import cl.smartlogix.envios.entity.EstadoEnvio;
import cl.smartlogix.envios.repository.EnvioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class PedidoAprobadoConsumer {

    private final EnvioRepository envioRepository;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_ENVIOS)
    @Transactional
    public void handlePedidoAprobado(PedidoAprobadoEventDTO event, Message message) {
        String correlationId = (String) message.getMessageProperties().getHeader("X-Correlation-Id");
        if (correlationId != null) {
            MDC.put("correlationId", correlationId);
        }
        try {
            log.info("📥 [EVENTO RECIBIDO] Generando despacho para Pedido ID: {}", event.getPedidoId());

            // Idempotencia: si ya existe un envío para este pedido, ignorar
            if (envioRepository.findByPedidoId(event.getPedidoId()).isPresent()) {
                log.warn("Ya existe un envío para el pedido {}. Evento duplicado ignorado.", event.getPedidoId());
                return;
            }

            Envio envio = Envio.builder()
                    .pedidoId(event.getPedidoId())
                    .usuarioId(event.getUsuarioId())
                    .destinatario(event.getDestinatario())
                    .calle(event.getCalle())
                    .numero(event.getNumero())
                    .comuna(event.getComuna())
                    .ciudad(event.getCiudad())
                    .codigoPostal(event.getCodigoPostal())
                    .metodoEnvio(event.getMetodoEnvio())
                    .pesoKg(event.getPesoKg())
                    .dimensiones(event.getDimensiones())
                    .estadoEnvio(EstadoEnvio.PENDIENTE)
                    .empresaLogistica("LOGIX_CARRIER_INTEGRATION")
                    .numeroTracking("TRK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                    .fechaEstimadaEntrega(LocalDate.now().plusDays(3))
                    .build();

            envioRepository.save(envio);
            log.info("🚚 [ENVÍO REGISTRADO] ID Físico: {}, Tracking: {}", envio.getId(), envio.getNumeroTracking());
        } catch (Exception e) {
            log.error("❌ [ERROR CRÍTICO] Fallo al registrar envío para Pedido ID: {}. Causa: {}", event.getPedidoId(),
                    e.getMessage());
            throw new AmqpRejectAndDontRequeueException(e.getMessage());
        } finally {
            MDC.remove("correlationId");
        }
    }
}