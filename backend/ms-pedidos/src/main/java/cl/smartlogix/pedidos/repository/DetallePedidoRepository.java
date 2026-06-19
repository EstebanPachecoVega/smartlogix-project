package cl.smartlogix.pedidos.repository;

import cl.smartlogix.pedidos.entity.DetallePedido;
import cl.smartlogix.pedidos.dto.response.VentaPorProductoCantidadDTO;
import cl.smartlogix.pedidos.dto.response.VentaPorProductoResponseDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Long> {

    @Query("""
        SELECT new cl.smartlogix.pedidos.dto.response.VentaPorProductoResponseDTO(
            d.productoId, SUM(d.subtotal)
        )
        FROM DetallePedido d
        GROUP BY d.productoId
        ORDER BY SUM(d.subtotal) DESC
    """)
    List<VentaPorProductoResponseDTO> findVentasPorProducto();

    @Query("""
        SELECT new cl.smartlogix.pedidos.dto.response.VentaPorProductoCantidadDTO(
            d.productoId, SUM(d.cantidad)
        )
        FROM DetallePedido d
        GROUP BY d.productoId
        ORDER BY SUM(d.cantidad) DESC
    """)
    List<VentaPorProductoCantidadDTO> findCantidadPorProducto();
}
