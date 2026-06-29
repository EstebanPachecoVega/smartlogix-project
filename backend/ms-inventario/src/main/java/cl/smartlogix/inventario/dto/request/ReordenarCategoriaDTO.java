package cl.smartlogix.inventario.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ReordenarCategoriaDTO {
    @NotNull
    private Long id;

    @NotNull
    private Integer ordenVisual;
}
