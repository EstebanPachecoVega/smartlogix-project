package cl.smartlogix.inventario.service;

import cl.smartlogix.inventario.dto.request.ReservarStockRequestDTO;
import java.util.List;

public interface InventarioService {
    String reservarStockLote(List<ReservarStockRequestDTO> items, String reservaIdInput);

    void confirmarReserva(String reservaId, List<ReservarStockRequestDTO> items);

    void cancelarReserva(String reservaId, List<ReservarStockRequestDTO> items);

    void liberarStock(Long productoId, Integer cantidad, String reservaId);
}