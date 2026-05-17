package cl.smartlogix.inventario.messaging;

import cl.smartlogix.inventario.config.RabbitMQConfig;
import cl.smartlogix.inventario.dto.event.StockCompensacionEvent;
import cl.smartlogix.inventario.service.InventarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException; // 🚀 Importante
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventarioEventListener {

    private final InventarioService inventarioService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_LIBERAR_STOCK)
    public void procesarCompensacionStock(StockCompensacionEvent event) {
        log.info("📥 [EVENTO RECIBIDO] Compensar stock del Pedido ID: {}", event.getPedidoId());
        
        try {
            inventarioService.liberarStock(event.getProductoId(), event.getCantidad());
            log.info("✅ [EVENTO PROCESADO] Stock restaurado exitosamente para Pedido ID: {}", event.getPedidoId());
            
        } catch (Exception e) {
            log.error("❌ [ERROR FATAL] Falló la compensación de stock para el Pedido ID: {}. Causa: {}", event.getPedidoId(), e.getMessage());
            
            // 🚀 ESTO ES LA MAGIA: Rechazamos el mensaje sin reencolarlo en la cola principal.
            // Como configuramos la DLQ en la cola, RabbitMQ lo atrapará y lo mandará directo al buzón de mensajes muertos.
            throw new AmqpRejectAndDontRequeueException("Enviando evento a la Dead Letter Queue", e);
        }
    }
}