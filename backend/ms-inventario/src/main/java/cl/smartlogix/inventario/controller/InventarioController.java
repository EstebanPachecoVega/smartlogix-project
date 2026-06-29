package cl.smartlogix.inventario.controller;

import cl.smartlogix.inventario.dto.request.CancelarReservaRequestDTO;
import cl.smartlogix.inventario.dto.request.ConfirmarReservaRequestDTO;
import cl.smartlogix.inventario.dto.request.PedidoStockRequestDTO;
import cl.smartlogix.inventario.dto.request.ReservarStockRequestDTO;
import cl.smartlogix.inventario.dto.response.StockResponseDTO;
import cl.smartlogix.inventario.entity.Producto;
import cl.smartlogix.inventario.exception.ResourceNotFoundException;
import cl.smartlogix.inventario.mapper.ProductoMapper;
import cl.smartlogix.inventario.repository.ProductoRepository;
import cl.smartlogix.inventario.service.InventarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/inventario")
@RequiredArgsConstructor
@Slf4j
public class InventarioController {

    private final InventarioService inventarioService;
    private final ProductoRepository productoRepository;
    private final ProductoMapper productoMapper;

    @PostMapping("/reservar")
    public ReservaResponseDTO reservarStock(@Valid @RequestBody PedidoStockRequestDTO request) {
        List<ReservarStockRequestDTO> items = request.getItems();
        String reservaId = inventarioService.reservarStockLote(items, request.getReservaId());
        return new ReservaResponseDTO(reservaId);
    }

    @PostMapping("/confirmar")
    @ResponseStatus(HttpStatus.OK)
    public void confirmarReserva(@Valid @RequestBody ConfirmarReservaRequestDTO request) {
        // Convertir items al formato que espera el servicio
        List<ReservarStockRequestDTO> items = request.getItems().stream()
                .map(i -> new ReservarStockRequestDTO(i.getProductoId(), i.getCantidad()))
                .collect(Collectors.toList());
        inventarioService.confirmarReserva(request.getReservaId(), items);
    }

    @PostMapping("/cancelar")
    @ResponseStatus(HttpStatus.OK)
    public void cancelarReserva(@Valid @RequestBody CancelarReservaRequestDTO request) {
        List<ReservarStockRequestDTO> items = request.getItems().stream()
                .map(i -> new ReservarStockRequestDTO(i.getProductoId(), i.getCantidad()))
                .collect(Collectors.toList());
        inventarioService.cancelarReserva(request.getReservaId(), items);
    }

    @GetMapping("/stock/{productoId}")
    public StockResponseDTO obtenerStockDisponible(@PathVariable Long productoId) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + productoId));
        return productoMapper.toStockResponseDTO(producto);
    }

    // DTO interno para respuesta de reserva
    record ReservaResponseDTO(String reservaId) {
    }
}