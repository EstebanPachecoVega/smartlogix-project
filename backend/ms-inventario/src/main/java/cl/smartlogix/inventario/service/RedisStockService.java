package cl.smartlogix.inventario.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisStockService {

    private final RedisTemplate<String, Object> redisTemplate;
    private DefaultRedisScript<Long> reservaScript;

    private static final String RESERVA_SCRIPT = "local stockKey = KEYS[1] " +
            "local reservaKey = KEYS[2] " +
            "local cantidad = tonumber(ARGV[1]) " +
            "local ttl = tonumber(ARGV[2]) " +
            "local stock = tonumber(redis.call('get', stockKey)) " +
            "if stock == nil then " +
            "    return -1 " +
            "end " +
            "if stock >= cantidad then " +
            "    redis.call('decrby', stockKey, cantidad) " +
            "    redis.call('setex', reservaKey, ttl, cantidad) " +
            "    return 1 " +
            "else " +
            "    return 0 " +
            "end";

    @PostConstruct
    public void init() {
        reservaScript = new DefaultRedisScript<>();
        reservaScript.setScriptText(RESERVA_SCRIPT);
        reservaScript.setResultType(Long.class);
    }

    public Boolean reservar(String reservaId, Long productoId, Integer cantidad, int ttlMinutos) {
        String stockKey = "stock:" + productoId;
        String reservaKey = "reserva:" + reservaId + ":" + productoId;
        Long result = redisTemplate.execute(
                reservaScript,
                List.of(stockKey, reservaKey),
                String.valueOf(cantidad),
                String.valueOf(TimeUnit.MINUTES.toSeconds(ttlMinutos)));
        if (result == null)
            return false;
        if (result == -1)
            return null;
        return result == 1;
    }

    public Integer confirmarReserva(String reservaId, Long productoId) {
        String reservaKey = "reserva:" + reservaId + ":" + productoId;
        Object cantidadObj = redisTemplate.opsForValue().get(reservaKey);
        if (cantidadObj == null)
            return null;
        Integer cantidad = (Integer) cantidadObj;
        redisTemplate.delete(reservaKey);
        return cantidad;
    }

    public boolean cancelarReserva(String reservaId, Long productoId) {
        String reservaKey = "reserva:" + reservaId + ":" + productoId;
        Object cantidadObj = redisTemplate.opsForValue().get(reservaKey);
        if (cantidadObj == null)
            return false;
        Integer cantidad = (Integer) cantidadObj;
        String stockKey = "stock:" + productoId;
        redisTemplate.opsForValue().increment(stockKey, cantidad);
        redisTemplate.delete(reservaKey);
        return true;
    }

    public void inicializarStock(Long productoId, Integer stock) {
        redisTemplate.opsForValue().set("stock:" + productoId, stock);
    }

    public Integer obtenerStockRedis(Long productoId) {
        Object stock = redisTemplate.opsForValue().get("stock:" + productoId);
        return stock != null ? (Integer) stock : null;
    }
}