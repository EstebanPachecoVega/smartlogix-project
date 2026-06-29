package cl.smartlogix.bff.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EnvioResponseDTO {
    private Long id;
    private Long pedidoId;
    private String numeroTracking;
    private String estadoEnvio;
    private String destinatario;
    private String fechaCreacion;
    private String calle;
    private String numero;
    private String comuna;
    private String ciudad;
    private String metodoEnvio;
    private String empresaLogistica;
    private String fechaEstimadaEntrega;
}