package cl.smartlogix.inventario.messaging;

import cl.smartlogix.inventario.config.RabbitMQConfig;
import cl.smartlogix.inventario.dto.event.StockCompensacionEvent;
import cl.smartlogix.inventario.service.InventarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventarioEventListener {

    private final InventarioService inventarioService;

    // Este método escucha los eventos de compensación de stock que llegan a la cola
    // configurada en RabbitMQConfig
    @RabbitListener(queues = RabbitMQConfig.QUEUE_LIBERAR_STOCK)
    public void procesarCompensacionStock(StockCompensacionEvent event) {
        log.info("📥 [EVENTO RECIBIDO] Compensar stock de la Orden: {}", event.getNumeroOrden());
        try {
            inventarioService.liberarStock(event.getProductoId(), event.getCantidad());
            log.info("✅ [EVENTO PROCESADO] Se restauraron {} unidades del Producto ID: {} (Orden: {})",
                    event.getCantidad(), event.getProductoId(), event.getNumeroOrden());
        } catch (Exception e) {
            log.error("❌ [ERROR FATAL] Falló la compensación de stock para la Orden: {}. Causa: {}",
                    event.getNumeroOrden(), e.getMessage());

            throw new AmqpRejectAndDontRequeueException(e.getMessage());
        }
    }
}