package cl.smartlogix.bff.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaResponseDTO {
    private Long id;
    private String nombre;
    private String slug;
    private String descripcion;
    private Long padreId;
    private String padreNombre;
    private Integer ordenVisual;
    private Boolean activo;
    private String fechaCreacion;
    private String fechaActualizacion;
}
