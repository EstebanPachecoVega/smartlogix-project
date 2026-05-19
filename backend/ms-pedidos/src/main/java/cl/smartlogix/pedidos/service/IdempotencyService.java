package cl.smartlogix.pedidos.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class IdempotencyService {

    private final RedisTemplate<String, String> redisTemplate;
    private final long ttlMinutes;

    public IdempotencyService(RedisTemplate<String, String> redisTemplate,
                              @Value("${pedido.idempotencia.ttl-minutes:30}") long ttlMinutes) {
        this.redisTemplate = redisTemplate;
        this.ttlMinutes = ttlMinutes;
    }

    public boolean isProcessed(String idempotencyKey) {
        String key = "idempotency:" + idempotencyKey;
        Boolean exists = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(exists);
    }

    public void markProcessed(String idempotencyKey) {
        String key = "idempotency:" + idempotencyKey;
        redisTemplate.opsForValue().set(key, "processed", ttlMinutes, TimeUnit.MINUTES);
    }
}