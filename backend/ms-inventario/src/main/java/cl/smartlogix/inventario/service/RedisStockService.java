package cl.smartlogix.inventario.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisStockService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY_MS = 50;

    @CircuitBreaker(name = "redis", fallbackMethod = "fallbackReservar")
    public Boolean reservar(String reservaId, Long productoId, Integer cantidad, int ttlMinutos) {
        // Verificar si la reserva ya existe (idempotencia)
        String reservaKey = "reserva:" + reservaId + ":" + productoId;
        Boolean existe = redisTemplate.hasKey(reservaKey);
        if (Boolean.TRUE.equals(existe)) {
            log.warn("La reserva con ID {} para producto {} ya existe. Se ignora.", reservaId, productoId);
            return true; // Considerar como éxito para no romper el flujo
        }

        String stockKey = "stock:" + productoId;

        for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
            try {
                Boolean result = redisTemplate.execute(new SessionCallback<Boolean>() {
                    @Override
                    @SuppressWarnings("unchecked")
                    public Boolean execute(RedisOperations operations) throws DataAccessException {
                        operations.watch(stockKey);
                        Object stockObj = operations.opsForValue().get(stockKey);
                        if (stockObj == null) {
                            operations.unwatch();
                            return null; // producto no sincronizado
                        }
                        Integer stock = Integer.valueOf(stockObj.toString());
                        if (stock >= cantidad) {
                            operations.multi();
                            operations.opsForValue().decrement(stockKey, cantidad);
                            operations.opsForValue().set(reservaKey, cantidad,
                                    TimeUnit.MINUTES.toSeconds(ttlMinutos), TimeUnit.SECONDS);
                            List<Object> results = operations.exec();
                            if (results != null && !results.isEmpty()) {
                                return true;
                            } else {
                                return false; // conflicto (concurrencia)
                            }
                        } else {
                            operations.unwatch();
                            return false; // stock insuficiente
                        }
                    }
                });
                if (result != null) {
                    return result;
                }
                // Conflicto detectado, reintentar
                if (attempt < MAX_RETRIES - 1) {
                    Thread.sleep(RETRY_DELAY_MS);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            } catch (Exception e) {
                log.error("Error en reserva atómica: {}", e.getMessage(), e);
                return false;
            }
        }
        return false;
    }

    public Integer confirmarReserva(String reservaId, Long productoId) {
        String reservaKey = "reserva:" + reservaId + ":" + productoId;
        Object cantidadObj = redisTemplate.opsForValue().get(reservaKey);
        if (cantidadObj == null)
            return null;
        Integer cantidad = Integer.valueOf(cantidadObj.toString());
        redisTemplate.delete(reservaKey);
        return cantidad;
    }

    public boolean cancelarReserva(String reservaId, Long productoId) {
        String reservaKey = "reserva:" + reservaId + ":" + productoId;
        String stockKey = "stock:" + productoId;
        Object cantidadObj = redisTemplate.opsForValue().get(reservaKey);
        if (cantidadObj == null)
            return false;
        Integer cantidad = Integer.valueOf(cantidadObj.toString());
        redisTemplate.opsForValue().increment(stockKey, cantidad);
        redisTemplate.delete(reservaKey);
        return true;
    }

    public void inicializarStock(Long productoId, Integer stock) {
        redisTemplate.opsForValue().set("stock:" + productoId, stock);
        log.debug("Producto {} inicializado en Redis con stock {}", productoId, stock);
    }

    public void eliminarStock(Long productoId) {
        redisTemplate.delete("stock:" + productoId);
        log.debug("Producto {} eliminado de Redis", productoId);
    }

    public Integer obtenerStockRedis(Long productoId) {
        Object stock = redisTemplate.opsForValue().get("stock:" + productoId);
        return stock != null ? Integer.valueOf(stock.toString()) : null;
    }

    // Fallback method for Circuit Breaker
    public Boolean fallbackReservar(String reservaId, Long productoId, Integer cantidad, int ttlMinutos, Throwable t) {
        log.error("Circuit Breaker abierto para reservar: {}", t.getMessage());
        return false;
    }
}