package cl.smartlogix.inventario.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MapaCategoriaResponseDTO {
    private Long productoId;
    private String categoriaNombre;
}
