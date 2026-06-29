package cl.smartlogix.pedidos.repository;

import cl.smartlogix.pedidos.entity.EstadoPedido;
import cl.smartlogix.pedidos.entity.Pedido;
import cl.smartlogix.pedidos.dto.response.ComparacionAnualResponseDTO;
import cl.smartlogix.pedidos.dto.response.VentasPlataformaResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByEstado(EstadoPedido estado);

    @EntityGraph(attributePaths = { "detalles" })
    Page<Pedido> findByEstado(EstadoPedido estado, Pageable pageable);

    @EntityGraph(attributePaths = { "detalles" })
    List<Pedido> findByUsuarioId(String usuarioId);

    @EntityGraph(attributePaths = { "detalles" })
    Page<Pedido> findByUsuarioId(String usuarioId, Pageable pageable);

    @EntityGraph(attributePaths = { "detalles" })
    List<Pedido> findAll();

    @EntityGraph(attributePaths = { "detalles" })
    Page<Pedido> findAll(Pageable pageable);

    @EntityGraph(attributePaths = { "detalles" })
    Optional<Pedido> findById(Long id);

    @EntityGraph(attributePaths = { "detalles" })
    Optional<Pedido> findByNumeroOrden(String numeroOrden);

    @Query("""
        SELECT new cl.smartlogix.pedidos.dto.response.VentasPlataformaResponseDTO(
            p.plataforma, SUM(p.totalCompra)
        )
        FROM Pedido p
        WHERE p.plataforma IS NOT NULL
          AND p.fechaPedido >= :desde
        GROUP BY p.plataforma
    """)
    List<VentasPlataformaResponseDTO> findVentasPorPlataforma(@Param("desde") java.time.LocalDateTime desde);

    @Query("""
        SELECT new cl.smartlogix.pedidos.dto.response.ComparacionAnualResponseDTO(
            MONTH(p.fechaPedido),
            SUM(CASE WHEN YEAR(p.fechaPedido) = :anioActual THEN p.totalCompra ELSE 0 END),
            SUM(CASE WHEN YEAR(p.fechaPedido) = :anioAnterior THEN p.totalCompra ELSE 0 END)
        )
        FROM Pedido p
        WHERE YEAR(p.fechaPedido) IN (:anioActual, :anioAnterior)
        GROUP BY MONTH(p.fechaPedido)
        ORDER BY MONTH(p.fechaPedido)
    """)
    List<ComparacionAnualResponseDTO> findComparacionAnual(
        @Param("anioActual") int anioActual,
        @Param("anioAnterior") int anioAnterior
    );
}