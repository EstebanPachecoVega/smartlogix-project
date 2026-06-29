package cl.smartlogix.inventario.messaging;

import cl.smartlogix.inventario.publisher.ReservaExpiradaPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RedisKeyExpirationListener extends KeyExpirationEventMessageListener {

    private final ReservaExpiradaPublisher reservaExpiradaPublisher;

    public RedisKeyExpirationListener(RedisMessageListenerContainer listenerContainer,
            ReservaExpiradaPublisher reservaExpiradaPublisher) {
        super(listenerContainer);
        this.reservaExpiradaPublisher = reservaExpiradaPublisher;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = message.toString();
        if (expiredKey.startsWith("reserva:")) {
            // Formato: reserva:{reservaId}:{productoId}
            String[] parts = expiredKey.split(":");
            if (parts.length >= 3) {
                String reservaId = parts[1];
                log.info("Reserva expirada en Redis: reservaId={}", reservaId);
                reservaExpiradaPublisher.publicarReservaExpirada(reservaId);
            } else {
                log.warn("Clave de reserva con formato inesperado: {}", expiredKey);
            }
        }
    }
}