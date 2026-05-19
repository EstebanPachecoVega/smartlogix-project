package cl.smartlogix.pedidos.client;

import cl.smartlogix.pedidos.dto.request.*;
import cl.smartlogix.pedidos.dto.response.PedidoStockResponseDTO;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "ms-inventario", url = "${inventario.url}", configuration = FeignErrorDecoder.class)
public interface InventarioClient {

    @PostMapping("/api/inventario/reservar")
    PedidoStockResponseDTO reservarStock(@Valid @RequestBody PedidoStockRequestDTO request);

    @PostMapping("/api/inventario/confirmar")
    void confirmarReserva(@Valid @RequestBody ConfirmarReservaRequestDTO request);

    @PostMapping("/api/inventario/cancelar")
    void cancelarReserva(@Valid @RequestBody CancelarReservaRequestDTO request);
}