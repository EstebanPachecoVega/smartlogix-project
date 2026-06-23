package cl.smartlogix.inventario.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.ValueOperations;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisStockServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private RedisOperations<String, Object> redisOps;

    @Mock
    private ValueOperations<String, Object> valueOps;

    private RedisStockService redisStockService;

    @BeforeEach
    void setUp() {
        redisStockService = new RedisStockService(redisTemplate);
    }

    @Test
    void reservar_ok() {
        when(redisTemplate.hasKey("reserva:res-1:1")).thenReturn(false);
        when(redisTemplate.execute(any(SessionCallback.class))).thenReturn(true);

        Boolean result = redisStockService.reservar("res-1", 1L, 5, 10);

        assertThat(result).isTrue();
        verify(redisTemplate).hasKey("reserva:res-1:1");
    }

    @Test
    void reservar_reservaYaExiste_retornaTrue() {
        when(redisTemplate.hasKey("reserva:res-1:1")).thenReturn(true);

        Boolean result = redisStockService.reservar("res-1", 1L, 5, 10);

        assertThat(result).isTrue();
        verify(redisTemplate, never()).execute(any(SessionCallback.class));
    }

    @Test
    void reservar_ejecutaFallido_retornaFalse() {
        when(redisTemplate.hasKey("reserva:res-1:1")).thenReturn(false);
        when(redisTemplate.execute(any(SessionCallback.class))).thenReturn(null);

        Boolean result = redisStockService.reservar("res-1", 1L, 5, 10);

        assertThat(result).isFalse();
    }

    @Test
    void confirmarReserva_ok() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("reserva:res-1:1")).thenReturn("5");

        Integer result = redisStockService.confirmarReserva("res-1", 1L);

        assertThat(result).isEqualTo(5);
        verify(redisTemplate).delete("reserva:res-1:1");
    }

    @Test
    void confirmarReserva_noExiste_retornaNull() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("reserva:res-1:1")).thenReturn(null);

        Integer result = redisStockService.confirmarReserva("res-1", 1L);

        assertThat(result).isNull();
        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    void cancelarReserva_ok() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("reserva:res-1:1")).thenReturn("5");

        boolean result = redisStockService.cancelarReserva("res-1", 1L);

        assertThat(result).isTrue();
        verify(valueOps).increment("stock:1", 5);
        verify(redisTemplate).delete("reserva:res-1:1");
    }

    @Test
    void cancelarReserva_noExiste_retornaFalse() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("reserva:res-1:1")).thenReturn(null);

        boolean result = redisStockService.cancelarReserva("res-1", 1L);

        assertThat(result).isFalse();
        verify(valueOps, never()).increment(anyString(), anyInt());
        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    void inicializarStock_guardaEnRedis() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);

        redisStockService.inicializarStock(1L, 100);

        verify(valueOps).set("stock:1", 100);
    }

    @Test
    void eliminarStock_eliminaDeRedis() {
        redisStockService.eliminarStock(1L);

        verify(redisTemplate).delete("stock:1");
    }

    @Test
    void obtenerStockRedis_existe() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("stock:1")).thenReturn("50");

        Integer result = redisStockService.obtenerStockRedis(1L);

        assertThat(result).isEqualTo(50);
    }

    @Test
    void obtenerStockRedis_noExiste_retornaNull() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("stock:1")).thenReturn(null);

        Integer result = redisStockService.obtenerStockRedis(1L);

        assertThat(result).isNull();
    }

    @Test
    void fallbackReservar_retornaFalse() {
        Boolean result = redisStockService.fallbackReservar("res-1", 1L, 5, 10, new RuntimeException("CB open"));

        assertThat(result).isFalse();
    }

    @Test
    void reservar_sessionCallback_stockNoSincronizado_retornaFalse() {
        when(redisTemplate.hasKey("reserva:res-1:1")).thenReturn(false);
        doAnswer(invocation -> {
            SessionCallback<Boolean> cb = invocation.getArgument(0);
            return cb.execute(redisOps);
        }).when(redisTemplate).execute(any(SessionCallback.class));
        doNothing().when(redisOps).watch(anyString());
        when(redisOps.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("stock:1")).thenReturn(null);
        doNothing().when(redisOps).unwatch();

        Boolean result = redisStockService.reservar("res-1", 1L, 5, 10);

        assertThat(result).isFalse();
        verify(redisOps, atLeastOnce()).unwatch();
        verify(redisOps, never()).multi();
        verify(redisOps, never()).exec();
    }

    @Test
    void reservar_sessionCallback_stockSuficiente_retornaTrue() {
        when(redisTemplate.hasKey("reserva:res-1:1")).thenReturn(false);
        doAnswer(invocation -> {
            SessionCallback<Boolean> cb = invocation.getArgument(0);
            return cb.execute(redisOps);
        }).when(redisTemplate).execute(any(SessionCallback.class));
        doNothing().when(redisOps).watch(anyString());
        when(redisOps.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("stock:1")).thenReturn("10");
        doNothing().when(redisOps).multi();
        when(valueOps.decrement(anyString(), anyLong())).thenReturn(1L);
        doNothing().when(valueOps).set(anyString(), anyInt(), anyLong(), any());
        when(redisOps.exec()).thenReturn(List.of("OK"));

        Boolean result = redisStockService.reservar("res-1", 1L, 5, 10);

        assertThat(result).isTrue();
        verify(redisOps).multi();
        verify(redisOps).exec();
        verify(valueOps).decrement("stock:1", 5);
    }

    @Test
    void reservar_sessionCallback_stockInsuficiente_retornaFalse() {
        when(redisTemplate.hasKey("reserva:res-1:1")).thenReturn(false);
        doAnswer(invocation -> {
            SessionCallback<Boolean> cb = invocation.getArgument(0);
            return cb.execute(redisOps);
        }).when(redisTemplate).execute(any(SessionCallback.class));
        doNothing().when(redisOps).watch(anyString());
        when(redisOps.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("stock:1")).thenReturn("3");
        doNothing().when(redisOps).unwatch();

        Boolean result = redisStockService.reservar("res-1", 1L, 5, 10);

        assertThat(result).isFalse();
        verify(redisOps).unwatch();
        verify(redisOps, never()).multi();
        verify(redisOps, never()).exec();
    }

    @Test
    void reservar_executeLanzaExcepcion_retornaFalse() {
        when(redisTemplate.hasKey("reserva:res-1:1")).thenReturn(false);
        when(redisTemplate.execute(any(SessionCallback.class))).thenThrow(new RuntimeException("Redis error"));

        Boolean result = redisStockService.reservar("res-1", 1L, 5, 10);

        assertThat(result).isFalse();
    }

    @Test
    void reservar_sessionCallback_conflictoOptimista_retornaFalse() {
        when(redisTemplate.hasKey("reserva:res-1:1")).thenReturn(false);
        doAnswer(invocation -> {
            SessionCallback<Boolean> cb = invocation.getArgument(0);
            return cb.execute(redisOps);
        }).when(redisTemplate).execute(any(SessionCallback.class));
        doNothing().when(redisOps).watch(anyString());
        when(redisOps.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("stock:1")).thenReturn("10");
        doNothing().when(redisOps).multi();
        when(valueOps.decrement(anyString(), anyLong())).thenReturn(1L);
        doNothing().when(valueOps).set(anyString(), anyInt(), anyLong(), any());
        when(redisOps.exec()).thenReturn(null);

        Boolean result = redisStockService.reservar("res-1", 1L, 5, 10);

        assertThat(result).isFalse();
        verify(redisOps).multi();
        verify(redisOps).exec();
    }
}
