package cl.smartlogix.pedidos.client;

import cl.smartlogix.pedidos.dto.request.ConfirmarReservaRequestDTO;
import cl.smartlogix.pedidos.dto.request.CancelarReservaRequestDTO;
import cl.smartlogix.pedidos.dto.request.ReservarStockRequestDTO;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "ms-inventario", url = "${inventario.url}", configuration = FeignErrorDecoder.class)
public interface InventarioClient {

    @PostMapping("/api/inventario/reservar")
    ReservaResponseDTO reservarStock(@Valid @RequestBody ReservarStockRequestDTO request);

    @PostMapping("/api/inventario/confirmar")
    void confirmarReserva(@Valid @RequestBody ConfirmarReservaRequestDTO request);

    @PostMapping("/api/inventario/cancelar")
    void cancelarReserva(@Valid @RequestBody CancelarReservaRequestDTO request);

    // DTO de respuesta de reserva
    class ReservaResponseDTO {
        private String reservaId;
        public String getReservaId() { return reservaId; }
        public void setReservaId(String reservaId) { this.reservaId = reservaId; }
    }
}