package cl.smartlogix.pedidos.repository;

import cl.smartlogix.pedidos.entity.DetallePedido;
import cl.smartlogix.pedidos.dto.response.VentaPorProductoCantidadDTO;
import cl.smartlogix.pedidos.dto.response.VentaPorProductoResponseDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Long> {

    @Query("""
        SELECT new cl.smartlogix.pedidos.dto.response.VentaPorProductoResponseDTO(
            d.productoId, SUM(d.subtotal)
        )
        FROM DetallePedido d
        WHERE d.pedido.fechaPedido >= :desde
        GROUP BY d.productoId
        ORDER BY SUM(d.subtotal) DESC
    """)
    List<VentaPorProductoResponseDTO> findVentasPorProducto(@Param("desde") LocalDateTime desde);

    @Query("""
        SELECT new cl.smartlogix.pedidos.dto.response.VentaPorProductoCantidadDTO(
            d.productoId, SUM(d.cantidad)
        )
        FROM DetallePedido d
        WHERE d.pedido.fechaPedido >= :desde
        GROUP BY d.productoId
        ORDER BY SUM(d.cantidad) DESC
    """)
    List<VentaPorProductoCantidadDTO> findCantidadPorProducto(@Param("desde") LocalDateTime desde);
}
