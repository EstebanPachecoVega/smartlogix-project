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

    @NotNull
    private Long usuarioId;
    @NotBlank
    private String destinatario;
    @NotBlank
    private String calle;
    @NotBlank
    private String numero;
    @NotBlank
    private String comuna;
    @NotBlank
    private String ciudad;
    private String codigoPostal;
    @NotBlank
    private String metodoEnvio;
    private Double pesoKg;
    private String dimensiones;

    @NotNull
    private List<DetalleDTO> items;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetalleDTO {
        @NotNull
        private Long productoId;
        @NotBlank
        private String sku;
        @NotBlank
        private String nombreProducto;
        @NotNull
        @Min(0)
        private Integer precioUnitario;
        @NotNull
        @Min(1)
        private Integer cantidad;
        private String imagenPrincipal;
    }
}