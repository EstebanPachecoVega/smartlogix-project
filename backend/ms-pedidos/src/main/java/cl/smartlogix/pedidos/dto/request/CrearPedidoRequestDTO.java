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

    // --- CAMPOS LOGÍSTICOS OBLIGATORIOS ---
    @NotNull(message = "El ID del usuario es obligatorio.")
    private String usuarioId;

    @NotBlank(message = "El destinatario es obligatorio.")
    private String destinatario;

    @NotBlank(message = "La calle es obligatoria.")
    private String calle;

    @NotBlank(message = "El número es obligatorio.")
    private String numero;

    @NotBlank(message = "La comuna es obligatoria.")
    private String comuna;

    @NotBlank(message = "La ciudad es obligatoria.")
    private String ciudad;

    private String codigoPostal;

    @NotBlank(message = "El método de envío es obligatorio.")
    private String metodoEnvio;

    private Double pesoKg;
    private String dimensiones;
    // ---------------------------------------------

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