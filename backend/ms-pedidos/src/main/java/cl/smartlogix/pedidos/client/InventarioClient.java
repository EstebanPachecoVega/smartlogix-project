package cl.smartlogix.pedidos.client;

import cl.smartlogix.pedidos.dto.request.*;
import cl.smartlogix.pedidos.dto.response.PedidoStockResponseDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "ms-inventario", url = "${inventario.url}", configuration = FeignErrorDecoder.class)
public interface InventarioClient {

    @PostMapping("/api/inventario/reservar")
    @CircuitBreaker(name = "inventario", fallbackMethod = "fallbackReservarStock")
    PedidoStockResponseDTO reservarStock(@Valid @RequestBody PedidoStockRequestDTO request);

    @PostMapping("/api/inventario/confirmar")
    @CircuitBreaker(name = "inventario", fallbackMethod = "fallbackConfirmarReserva")
    void confirmarReserva(@Valid @RequestBody ConfirmarReservaRequestDTO request);

    @PostMapping("/api/inventario/cancelar")
    @CircuitBreaker(name = "inventario", fallbackMethod = "fallbackCancelarReserva")
    void cancelarReserva(@Valid @RequestBody CancelarReservaRequestDTO request);

    default PedidoStockResponseDTO fallbackReservarStock(PedidoStockRequestDTO request, Throwable t) {
        throw new RuntimeException("Servicio de inventario no disponible para reservar stock: " + t.getMessage());
    }

    default void fallbackConfirmarReserva(ConfirmarReservaRequestDTO request, Throwable t) {
        throw new RuntimeException("Servicio de inventario no disponible para confirmar reserva: " + t.getMessage());
    }

    default void fallbackCancelarReserva(CancelarReservaRequestDTO request, Throwable t) {
        throw new RuntimeException("Servicio de inventario no disponible para cancelar reserva: " + t.getMessage());
    }
}