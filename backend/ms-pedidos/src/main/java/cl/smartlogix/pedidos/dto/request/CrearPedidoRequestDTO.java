package cl.smartlogix.pedidos.dto.request;

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
public class CrearPedidoRequestDTO {

    @NotEmpty(message = "El pedido debe contener al menos un producto.")
    @Valid
    private List<DetalleRequestDTO> items;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetalleRequestDTO {
        
        @NotNull(message = "El ID del producto es obligatorio.")
        private Long productoId;

        @NotBlank(message = "El SKU del producto es obligatorio.")
        private String sku;

        @NotBlank(message = "El nombre del producto es obligatorio.")
        private String nombreProducto;

        @NotNull(message = "El precio unitario es obligatorio.")
        @Min(value = 0, message = "El precio unitario no puede ser negativo.")
        private Integer precioUnitario;

        @NotNull(message = "La cantidad es obligatoria.")
        @Min(value = 1, message = "La cantidad mínima debe ser al menos 1.")
        private Integer cantidad;
    }
}