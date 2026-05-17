package cl.smartlogix.inventario.controller;

import cl.smartlogix.inventario.dto.request.ReservarStockRequestDTO;
import cl.smartlogix.inventario.dto.response.StockResponseDTO;
import cl.smartlogix.inventario.entity.Producto;
import cl.smartlogix.inventario.exception.ResourceNotFoundException;
import cl.smartlogix.inventario.repository.ProductoRepository;
import cl.smartlogix.inventario.service.InventarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventario")
@RequiredArgsConstructor
@Slf4j
public class InventarioController {

    private final InventarioService inventarioService;
    private final ProductoRepository productoRepository;

    // Endpoint para reservar stock de un producto
    @PostMapping("/reservar")
    @ResponseStatus(HttpStatus.OK)
    public void reservarStock(@Valid @RequestBody ReservarStockRequestDTO request) {
        log.info("REST Request - Reservar {} unidades del producto ID: {}", request.getCantidad(),
                request.getProductoId());
        inventarioService.reservarStock(request.getProductoId(), request.getCantidad());
    }

    // Endpoint para consultar el stock disponible de un producto
    @GetMapping("/stock/{productoId}")
    public StockResponseDTO obtenerStockDisponible(@PathVariable Long productoId) {
        log.debug("REST Request - Consultar stock disponible para producto ID: {}", productoId);
        return productoRepository.findById(productoId)
                .map(producto -> new StockResponseDTO(producto.getId(), producto.getCantidad()))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Producto no encontrado para verificar stock con ID: " + productoId));
    }

    // Endpoint para liberar o devolver stock de un producto
    @PostMapping("/liberar")
    @ResponseStatus(HttpStatus.OK)
    public void liberarStock(@Valid @RequestBody ReservarStockRequestDTO request) {
        log.info("REST Request - Liberar / Devolver {} unidades al producto ID: {}", request.getCantidad(),
                request.getProductoId());
        inventarioService.liberarStock(request.getProductoId(), request.getCantidad());
    }
}