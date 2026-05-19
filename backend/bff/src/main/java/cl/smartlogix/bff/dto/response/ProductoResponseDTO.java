package cl.smartlogix.bff.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ProductoResponseDTO {
    private Long id;
    private String nombre;
    private Integer precio;
    private Integer cantidad;
    private String imagenPrincipal;
}