package cl.smartlogix.inventario.service;

public interface InventarioService {
    void reservarStock(Long productoId, Integer cantidad);
}