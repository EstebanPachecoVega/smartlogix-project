package cl.smartlogix.envios.repository;

import cl.smartlogix.envios.entity.Envio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnvioRepository extends JpaRepository<Envio, Long> {
}