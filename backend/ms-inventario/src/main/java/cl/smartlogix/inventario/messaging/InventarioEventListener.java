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

    @RabbitListener(queues = RabbitMQConfig.QUEUE_LIBERAR_STOCK)
    public void procesarCompensacionStock(StockCompensacionEvent event) {
        log.info("Evento compensación: liberar stock de orden {}", event.getNumeroOrden());
        try {
            inventarioService.liberarStock(event.getProductoId(), event.getCantidad(), event.getReservaId());
            log.info("Stock liberado para producto {} (reservaId {})", event.getProductoId(), event.getReservaId());
        } catch (Exception e) {
            log.error("Falló liberación de stock para orden {}", event.getNumeroOrden(), e);
            throw new AmqpRejectAndDontRequeueException(e.getMessage());
        }
    }
}