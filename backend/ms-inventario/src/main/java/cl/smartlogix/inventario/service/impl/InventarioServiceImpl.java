package cl.smartlogix.inventario.service.impl;

import cl.smartlogix.inventario.entity.Producto;
import cl.smartlogix.inventario.exception.DomainException;
import cl.smartlogix.inventario.exception.ResourceNotFoundException;
import cl.smartlogix.inventario.repository.ProductoRepository;
import cl.smartlogix.inventario.service.InventarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventarioServiceImpl implements InventarioService {
    private final ProductoRepository productoRepository;

    // Reserva de stock con manejo de condiciones de carrera utilizando una consulta
    // de actualización atómica
    @Override
    @Transactional
    public void reservarStock(Long productoId, Integer cantidad) {
        log.debug("Intentando reservar stock atómicamente para producto {} cantidad {}", productoId, cantidad);
        int filasActualizadas = productoRepository.restarStockAtomico(productoId, cantidad);
        if (filasActualizadas == 0) {
            boolean existe = productoRepository.existsById(productoId);
            if (!existe) {
                throw new ResourceNotFoundException("Producto con ID " + productoId + " no encontrado");
            } else {
                throw new DomainException(
                        "Stock insuficiente o condición de carrera detectada para la cantidad: " + cantidad);
            }
        }
        log.info("Stock reservado correctamente de forma atómica para producto {}.", productoId);
    }

    // Liberación o devolución de stock con validación de cantidad y manejo de
    // excepciones
    @Override
    @Transactional
    public void liberarStock(Long productoId, Integer cantidad) {
        log.debug("Intentando liberar/devolver stock para producto {} cantidad {}", productoId, cantidad);
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se pudo revertir el stock. Producto con ID " + productoId + " no encontrado"));
        if (cantidad <= 0) {
            throw new DomainException("La cantidad a liberar debe ser mayor a cero. Solicitado: " + cantidad);
        }
        // Devolvemos las unidades al stock actual
        int nuevoStock = producto.getCantidad() + cantidad;
        producto.setCantidad(nuevoStock);
        productoRepository.save(producto);
        log.info("Stock liberado/devuelto correctamente para producto {}. Nuevo stock disponible: {}", productoId,
                nuevoStock);
    }
}