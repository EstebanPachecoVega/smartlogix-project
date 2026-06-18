package cl.smartlogix.bff.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ProductoResponseDTO {
    private Long id;
    private String sku;
    private String nombre;
    private String slug;               
    private String descripcion;
    private Integer precio;
    private Integer cantidad;
    private String imagenPrincipal;
    private List<String> imagenes;
    private String fechaCreacion;
    private String fechaActualizacion;
    private Long categoriaId;
    private String categoriaNombre;
    private Boolean destacado;
    private Boolean novedad;
    private Boolean activo;
}