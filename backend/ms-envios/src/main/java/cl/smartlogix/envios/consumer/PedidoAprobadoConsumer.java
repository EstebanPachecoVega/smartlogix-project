package cl.smartlogix.envios.consumer;

import cl.smartlogix.envios.dto.event.PedidoAprobadoEventDTO;
import cl.smartlogix.envios.entity.Envio;
import cl.smartlogix.envios.entity.EstadoEnvio;
import cl.smartlogix.envios.repository.EnvioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class PedidoAprobadoConsumer {
    private final EnvioRepository envioRepository;

    @RabbitListener(queues = "envios.queue")
    @Transactional
    public void handlePedidoAprobado(PedidoAprobadoEventDTO event) {
        log.info("Recibido evento de pedido aprobado: {}", event);
        Envio envio = new Envio();
        envio.setPedidoId(event.getPedidoId());
        envio.setEstado(EstadoEnvio.CREADO);
        envioRepository.save(envio);
        log.info("Envío creado con id {} para el pedido {}", envio.getId(), event.getPedidoId());
    }
}