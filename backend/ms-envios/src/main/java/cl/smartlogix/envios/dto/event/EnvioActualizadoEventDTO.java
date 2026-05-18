package cl.smartlogix.envios.dto.event;

import cl.smartlogix.envios.entity.EstadoEnvio;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EnvioActualizadoEventDTO {
    private Long pedidoId;
    private Long envioId;
    private String numeroTracking;
    private EstadoEnvio estadoEnvio;
}