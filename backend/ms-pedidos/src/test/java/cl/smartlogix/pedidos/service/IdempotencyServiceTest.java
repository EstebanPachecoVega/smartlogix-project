package cl.smartlogix.pedidos.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IdempotencyServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOps;

    private IdempotencyService idempotencyService;

    @BeforeEach
    void setUp() {
        idempotencyService = new IdempotencyService(redisTemplate, 30L);
    }

    @Test
    void isProcessed_true() {
        when(redisTemplate.hasKey("idempotency:key-123")).thenReturn(true);

        boolean result = idempotencyService.isProcessed("key-123");

        assertThat(result).isTrue();
    }

    @Test
    void isProcessed_false() {
        when(redisTemplate.hasKey("idempotency:key-123")).thenReturn(false);

        boolean result = idempotencyService.isProcessed("key-123");

        assertThat(result).isFalse();
    }

    @Test
    void markProcessed_guardaEnRedis() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);

        idempotencyService.markProcessed("key-123");

        verify(valueOps).set("idempotency:key-123", "processed", 30L, TimeUnit.MINUTES);
    }
}
