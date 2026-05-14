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

    @Override
    @Transactional
    public void reservarStock(Long productoId, Integer cantidad) {
        log.debug("Intentando reservar stock para producto {} cantidad {}", productoId, cantidad);
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto con ID " + productoId + " no encontrado"));
        
        if (producto.getStock() < cantidad) {
            throw new DomainException("Stock insuficiente. Disponible: " + producto.getStock() + ", solicitado: " + cantidad);
        }
        
        producto.setStock(producto.getStock() - cantidad);
        productoRepository.save(producto);
        log.info("Stock reservado correctamente para producto {}. Nuevo stock: {}", productoId, producto.getStock());
    }
}