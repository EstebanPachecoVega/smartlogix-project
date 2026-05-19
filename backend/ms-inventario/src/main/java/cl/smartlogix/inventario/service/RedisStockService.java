package cl.smartlogix.inventario.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisStockService {

    private final RedisTemplate<String, Object> redisTemplate;

    // Método atómico usando WATCH/MULTI (evita scripts Lua)
    public Boolean reservar(String reservaId, Long productoId, Integer cantidad, int ttlMinutos) {
        String stockKey = "stock:" + productoId;
        String reservaKey = "reserva:" + reservaId + ":" + productoId;

        SessionCallback<Boolean> sessionCallback = new SessionCallback<>() {
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
                    operations.opsForValue().set(reservaKey, cantidad, TimeUnit.MINUTES.toSeconds(ttlMinutos), TimeUnit.SECONDS);
                    List<Object> results = operations.exec();
                    if (results != null && !results.isEmpty()) {
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    operations.unwatch();
                    return false;
                }
            }
        };

        try {
            return redisTemplate.execute(sessionCallback);
        } catch (Exception e) {
            log.error("Error en reserva atómica: {}", e.getMessage(), e);
            return false;
        }
    }

    // Confirmar reserva (elimina la clave de reserva y devuelve cantidad)
    public Integer confirmarReserva(String reservaId, Long productoId) {
        String reservaKey = "reserva:" + reservaId + ":" + productoId;
        Object cantidadObj = redisTemplate.opsForValue().get(reservaKey);
        if (cantidadObj == null) return null;
        Integer cantidad = Integer.valueOf(cantidadObj.toString());
        redisTemplate.delete(reservaKey);
        return cantidad;
    }

    // Cancelar reserva (devuelve stock y elimina reserva)
    public boolean cancelarReserva(String reservaId, Long productoId) {
        String reservaKey = "reserva:" + reservaId + ":" + productoId;
        String stockKey = "stock:" + productoId;
        Object cantidadObj = redisTemplate.opsForValue().get(reservaKey);
        if (cantidadObj == null) return false;
        Integer cantidad = Integer.valueOf(cantidadObj.toString());
        redisTemplate.opsForValue().increment(stockKey, cantidad);
        redisTemplate.delete(reservaKey);
        return true;
    }

    // Inicializar stock (sobrescribe)
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
}