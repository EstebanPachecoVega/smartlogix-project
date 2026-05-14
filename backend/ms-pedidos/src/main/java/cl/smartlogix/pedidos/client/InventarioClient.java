package cl.smartlogix.pedidos.client;

import cl.smartlogix.pedidos.dto.request.ReservarStockRequestDTO;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "ms-inventario", url = "${inventario.url}", configuration = FeignErrorDecoder.class)
public interface InventarioClient {
    @PostMapping("/api/inventario/reservar")
    void reservarStock(@Valid @RequestBody ReservarStockRequestDTO request);
}