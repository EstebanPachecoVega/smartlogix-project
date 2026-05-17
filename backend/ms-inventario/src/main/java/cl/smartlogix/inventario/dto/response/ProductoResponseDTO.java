package cl.smartlogix.inventario.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductoResponseDTO {
    private Long id;
    private String sku;
    private String nombre;
    private String slug;
    private String descripcion;
    private Long categoriaId;
    private String categoriaNombre;
    private Integer precio;
    private Integer cantidad;
    private String imagenPrincipal;
    private List<String> imagenes;
    private Boolean destacado;
    private Boolean novedad;
    private Boolean activo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}