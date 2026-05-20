package cl.smartlogix.bff.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaRequestDTO {
    private String nombre;
    private String slug;
    private String descripcion;
    private Long padreId;
    private Integer ordenVisual;
    private Boolean activo;
}