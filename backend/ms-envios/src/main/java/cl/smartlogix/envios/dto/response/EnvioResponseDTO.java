package cl.smartlogix.envios.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EnvioResponseDTO {
    private Long id;
    private Long pedidoId;
    private Long usuarioId;
    private String destinatario;
    private String calle;
    private String numero;
    private String comuna;
    private String ciudad;
    private String codigoPostal;
    private String metodoEnvio;
    private String empresaLogistica;
    private String numeroTracking;
    private LocalDate fechaEstimadaEntrega;
    private String estadoEnvio;
    private Double pesoKg;
    private String dimensiones;
    private LocalDateTime fechaCreacion;
}