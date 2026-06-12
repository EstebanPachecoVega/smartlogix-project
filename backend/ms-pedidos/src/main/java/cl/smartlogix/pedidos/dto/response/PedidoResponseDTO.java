package cl.smartlogix.pedidos.dto.response;

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
public class PedidoResponseDTO {
    private Long id;
    private String numeroOrden;
    private LocalDateTime fechaPedido;
    private String estado;
    private Integer totalCompra;
    private String destinatario;
    private String calle;
    private String numero;
    private String comuna;
    private String ciudad;
    private String codigoPostal;
    private String metodoEnvio;
    private List<DetalleResponseDTO> detalles;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetalleResponseDTO {
        private Long id;
        private Long productoId;
        private String sku;
        private String nombreProducto;
        private Integer precioUnitario;
        private Integer cantidad;
        private Integer subtotal;
        private String imagenPrincipal;
    }
}