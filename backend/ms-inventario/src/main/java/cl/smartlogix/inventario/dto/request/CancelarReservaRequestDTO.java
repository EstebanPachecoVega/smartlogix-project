package cl.smartlogix.inventario.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CancelarReservaRequestDTO {
    @NotBlank(message = "El ID de reserva es obligatorio")
    private String reservaId;

    @NotEmpty(message = "Debe incluir al menos un ítem")
    @Valid
    private List<ItemCancelacionDTO> items;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemCancelacionDTO {
        @NotNull(message = "El ID del producto es obligatorio")
        private Long productoId;

        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 1, message = "La cantidad debe ser al menos 1")
        private Integer cantidad;
    }
}