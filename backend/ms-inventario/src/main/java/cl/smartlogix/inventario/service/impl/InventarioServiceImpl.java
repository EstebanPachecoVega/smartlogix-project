package cl.smartlogix.inventario.service.impl;

import cl.smartlogix.inventario.dto.request.ReservarStockRequestDTO;
import cl.smartlogix.inventario.entity.Producto;
import cl.smartlogix.inventario.exception.DomainException;
import cl.smartlogix.inventario.exception.ResourceNotFoundException;
import cl.smartlogix.inventario.repository.ProductoRepository;
import cl.smartlogix.inventario.service.InventarioService;
import cl.smartlogix.inventario.service.RedisStockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventarioServiceImpl implements InventarioService {

    private final ProductoRepository productoRepository;
    private final RedisStockService redisStockService;
    private static final int RESERVA_TTL_MINUTOS = 10; // tiempo de reserva en Redis

    // Sincronización masiva al iniciar la aplicación
    @EventListener(ApplicationReadyEvent.class)
    @Transactional(readOnly = true)
    public void sincronizarStockInicial() {
        log.info("Iniciando sincronización masiva de stock desde MySQL a Redis...");
        List<Producto> productos = productoRepository.findAll();
        for (Producto producto : productos) {
            redisStockService.inicializarStock(producto.getId(), producto.getCantidad());
        }
        log.info("Sincronización completada. {} productos cargados en Redis.", productos.size());
    }

    // Reconciliación programada cada hora (opcional pero recomendada)
    @Scheduled(cron = "0 0 * * * *") // cada hora
    @Transactional(readOnly = true)
    public void reconciliarStock() {
        log.debug("Ejecutando reconciliación periódica de stock entre MySQL y Redis");
        List<Producto> productos = productoRepository.findAll();
        for (Producto producto : productos) {
            Integer stockRedis = redisStockService.obtenerStockRedis(producto.getId());
            if (stockRedis == null || !stockRedis.equals(producto.getCantidad())) {
                redisStockService.inicializarStock(producto.getId(), producto.getCantidad());
                log.warn("Reconciliado producto {}: Redis={}, BD={}", producto.getId(), stockRedis, producto.getCantidad());
            }
        }
    }

    @Override
    @Transactional
    public String reservarStockLote(List<ReservarStockRequestDTO> items, String reservaIdInput) {
        String reservaId = (reservaIdInput != null && !reservaIdInput.isBlank()) 
                ? reservaIdInput 
                : UUID.randomUUID().toString();
        log.debug("Reservando lote con reservaId: {}", reservaId);

        for (ReservarStockRequestDTO item : items) {
            Boolean reservado = redisStockService.reservar(reservaId, item.getProductoId(), item.getCantidad(), RESERVA_TTL_MINUTOS);
            if (reservado == null) {
                throw new DomainException("Producto " + item.getProductoId() + " no sincronizado en Redis");
            }
            if (!reservado) {
                // rollback parcial: cancelar los que ya se reservaron
                items.stream()
                        .limit(items.indexOf(item))
                        .forEach(i -> redisStockService.cancelarReserva(reservaId, i.getProductoId()));
                throw new DomainException("Stock insuficiente para producto " + item.getProductoId());
            }
        }
        log.info("Reserva creada en Redis con ID: {}", reservaId);
        return reservaId;
    }

    @Override
    @Transactional
    public void confirmarReserva(String reservaId, List<ReservarStockRequestDTO> items) {
        log.debug("Confirmando reserva {}", reservaId);
        for (ReservarStockRequestDTO item : items) {
            Integer cantidadReservada = redisStockService.confirmarReserva(reservaId, item.getProductoId());
            if (cantidadReservada == null) {
                throw new DomainException("Reserva " + reservaId + " para producto " + item.getProductoId() + " no existe o expiró");
            }
            int filas = productoRepository.restarStockAtomico(item.getProductoId(), cantidadReservada);
            if (filas == 0) {
                throw new DomainException("No se pudo descontar stock en BD para producto " + item.getProductoId());
            }
        }
        log.info("Reserva {} confirmada y stock descontado en BD", reservaId);
    }

    @Override
    @Transactional
    public void cancelarReserva(String reservaId, List<ReservarStockRequestDTO> items) {
        log.debug("Cancelando reserva {}", reservaId);
        for (ReservarStockRequestDTO item : items) {
            boolean cancelada = redisStockService.cancelarReserva(reservaId, item.getProductoId());
            if (!cancelada) {
                log.warn("No se pudo cancelar reserva para producto {} (puede que ya no exista)", item.getProductoId());
            }
        }
        log.info("Reserva {} cancelada", reservaId);
    }

    @Override
    @Transactional
    public void liberarStock(Long productoId, Integer cantidad, String reservaId) {
        log.debug("Liberando stock por compensación. Producto: {}, Cantidad: {}, ReservaId: {}", productoId, cantidad, reservaId);
        if (cantidad == null || cantidad <= 0) {
            throw new DomainException("La cantidad a liberar debe ser mayor a cero");
        }
        // Primero cancelar la reserva en Redis si existe
        if (reservaId != null && !reservaId.isBlank()) {
            redisStockService.cancelarReserva(reservaId, productoId);
        }
        // Luego sumar en BD (compensación)
        int filas = productoRepository.adicionarStockAtomico(productoId, cantidad);
        if (filas == 0 && !productoRepository.existsById(productoId)) {
            throw new ResourceNotFoundException("Producto no existe");
        }
        // Sincronizar Redis con el nuevo stock de BD
        productoRepository.findById(productoId).ifPresent(p -> redisStockService.inicializarStock(productoId, p.getCantidad()));
        log.info("Stock restaurado para producto {}: +{} unidades", productoId, cantidad);
    }
}