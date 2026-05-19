package cl.smartlogix.bff.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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

    @NotNull(message = "El ID del usuario es obligatorio")
    private Long usuarioId;

    @NotBlank(message = "El destinatario es obligatorio")
    private String destinatario;

    @NotBlank(message = "La calle es obligatoria")
    private String calle;

    @NotBlank(message = "El número es obligatorio")
    private String numero;

    @NotBlank(message = "La comuna es obligatoria")
    private String comuna;

    @NotBlank(message = "La ciudad es obligatoria")
    private String ciudad;

    private String codigoPostal;

    @NotBlank(message = "El método de envío es obligatorio")
    private String metodoEnvio;

    private Double pesoKg;
    private String dimensiones;

    @NotNull(message = "Debe incluir al menos un ítem")
    private List<DetalleDTO> items;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetalleDTO {
        @NotNull(message = "El ID del producto es obligatorio")
        private Long productoId;

        @NotBlank(message = "El SKU es obligatorio")
        private String sku;

        @NotBlank(message = "El nombre del producto es obligatorio")
        private String nombreProducto;

        @NotNull(message = "El precio unitario es obligatorio")
        @Min(value = 0, message = "El precio no puede ser negativo")
        private Integer precioUnitario;

        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 1, message = "La cantidad debe ser al menos 1")
        private Integer cantidad;
    }
}