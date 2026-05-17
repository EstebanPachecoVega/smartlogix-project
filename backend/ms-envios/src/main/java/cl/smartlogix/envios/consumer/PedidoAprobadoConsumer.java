package cl.smartlogix.envios.consumer;

import cl.smartlogix.envios.config.RabbitMQConfig;
import cl.smartlogix.envios.dto.event.PedidoAprobadoEventDTO;
import cl.smartlogix.envios.entity.Envio;
import cl.smartlogix.envios.repository.EnvioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
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
    public void handlePedidoAprobado(PedidoAprobadoEventDTO event) {
        log.info("📥 [EVENTO RECIBIDO] Generando despacho para Pedido ID: {}", event.getPedidoId());
        
        try {
            // 🚀 Construcción limpia y segura con Builder
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
                    .estadoEnvio("PREPARACION")
                    .empresaLogistica("LOGIX_CARRIER_INTEGRATION")
                    .numeroTracking("TRK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                    .fechaEstimadaEntrega(LocalDate.now().plusDays(3))
                    .build();

            envioRepository.save(envio);
            
            log.info("🚚 [ENVÍO REGISTRADO] ID Físico: {}, Tracking: {}", envio.getId(), envio.getNumeroTracking());
            
        } catch (Exception e) {
            log.error("❌ [ERROR CRÍTICO] Fallo al registrar envío para Pedido ID: {}. Causa: {}", event.getPedidoId(), e.getMessage());
            // 🚀 Dispara el mensaje a la DLQ para no perder el pedido del cliente
            throw new AmqpRejectAndDontRequeueException(e.getMessage()); 
        }
    }
}