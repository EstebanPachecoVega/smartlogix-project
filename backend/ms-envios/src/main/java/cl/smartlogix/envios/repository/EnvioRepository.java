package cl.smartlogix.envios.repository;

import cl.smartlogix.envios.entity.Envio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface EnvioRepository extends JpaRepository<Envio, Long> {
    Optional<Envio> findByPedidoId(Long pedidoId);
    Optional<Envio> findByNumeroTracking(String numeroTracking);
}