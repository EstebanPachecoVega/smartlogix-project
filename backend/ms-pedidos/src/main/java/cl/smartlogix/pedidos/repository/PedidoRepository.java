package cl.smartlogix.pedidos.repository;

import cl.smartlogix.pedidos.entity.EstadoPedido;
import cl.smartlogix.pedidos.entity.Pedido;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByEstado(EstadoPedido estado);

    @EntityGraph(attributePaths = { "detalles" })
    List<Pedido> findAll();

    @EntityGraph(attributePaths = { "detalles" })
    Optional<Pedido> findById(Long id);

    @EntityGraph(attributePaths = { "detalles" })
    Optional<Pedido> findByNumeroOrden(String numeroOrden);
}