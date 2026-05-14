package cl.smartlogix.inventario.controller;

import cl.smartlogix.inventario.dto.request.ReservarStockRequestDTO;
import cl.smartlogix.inventario.service.InventarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventario")
@RequiredArgsConstructor
public class InventarioController {
    private final InventarioService inventarioService;

    @PostMapping("/reservar")
    @ResponseStatus(HttpStatus.OK)
    public void reservarStock(@Valid @RequestBody ReservarStockRequestDTO request) {
        inventarioService.reservarStock(request.getProductoId(), request.getCantidad());
    }
}