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

    // Reserva de stock con validación de cantidad y manejo de excepciones para
    // condiciones de carrera
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
        log.debug("Iniciando liberación de stock por compensación. Producto: {}, Cantidad a devolver: {}", productoId,
                cantidad);
        if (cantidad == null || cantidad <= 0) {
            throw new DomainException("La cantidad a liberar debe ser mayor a cero");
        }
        int filasActualizadas = productoRepository.adicionarStockAtomico(productoId, cantidad);
        if (filasActualizadas == 0) {
            if (!productoRepository.existsById(productoId)) {
                throw new ResourceNotFoundException(
                        "No se pudo liberar stock. Producto con ID " + productoId + " no existe");
            }
            throw new DomainException("Error interno al intentar adicionar stock de forma atómica");
        }
        log.info("Stock restaurado correctamente. Producto ID: {}, {} unidades devueltas al inventario", productoId, cantidad);
    }
}