package cl.smartlogix.pedidos.dto.event;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnvioActualizadoEventDTO {
    private Long pedidoId;
    private Long envioId;
    private String estadoEnvio;
}