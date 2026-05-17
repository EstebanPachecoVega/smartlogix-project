package cl.smartlogix.inventario.service;

public interface InventarioService {
    void reservarStock(Long productoId, Integer cantidad);
    void liberarStock(Long productoId, Integer cantidad);
}