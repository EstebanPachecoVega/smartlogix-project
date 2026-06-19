package cl.smartlogix.pedidos.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VentasPlataformaResponseDTO {
    private String plataforma;
    private Long total;
}
