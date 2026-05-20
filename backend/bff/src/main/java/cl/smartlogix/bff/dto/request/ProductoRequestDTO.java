package cl.smartlogix.bff.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductoRequestDTO {
    private String sku;
    private String nombre;
    private String slug;
    private String descripcion;
    private Long categoriaId;
    private Integer precio;
    private Integer cantidad;
    private String imagenPrincipal;
    private List<String> imagenes;
    private Boolean destacado;
    private Boolean novedad;
    private Boolean activo;
}