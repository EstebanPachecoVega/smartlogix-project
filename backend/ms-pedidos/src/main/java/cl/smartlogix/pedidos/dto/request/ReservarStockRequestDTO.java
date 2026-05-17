package cl.smartlogix.pedidos.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReservarStockRequestDTO {

    @NotNull(message = "El ID del producto es requerido para la reserva.")
    private Long productoId;

    @NotNull(message = "La cantidad es requerida.")
    @Min(value = 1, message = "La cantidad a reservar debe ser mínimo 1.")
    private Integer cantidad;
}