package cl.smartlogix.envios.repository;

import cl.smartlogix.envios.entity.Envio;
import cl.smartlogix.envios.entity.EstadoEnvio;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface EnvioRepository extends JpaRepository<Envio, Long> {
    Optional<Envio> findByPedidoId(Long pedidoId);

    Optional<Envio> findByNumeroTracking(String numeroTracking);

    List<Envio> findByEstadoEnvio(EstadoEnvio estadoEnvio);

    Page<Envio> findByEstadoEnvio(EstadoEnvio estadoEnvio, Pageable pageable);

    List<Envio> findByEstadoEnvioIn(List<EstadoEnvio> estados);

    Page<Envio> findByEstadoEnvioIn(List<EstadoEnvio> estados, Pageable pageable);
}