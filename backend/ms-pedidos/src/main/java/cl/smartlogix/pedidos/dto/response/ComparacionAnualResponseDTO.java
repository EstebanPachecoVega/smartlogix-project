package cl.smartlogix.pedidos.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ComparacionAnualResponseDTO {
    private Integer mes;
    private Long añoActual;
    private Long añoAnterior;
}
