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
    private String estado;
}