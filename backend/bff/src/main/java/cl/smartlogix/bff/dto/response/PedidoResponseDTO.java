package cl.smartlogix.bff.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PedidoResponseDTO {
    private Long id;
    private String numeroOrden;
    private String estado;
    private Integer totalCompra;
    private String fechaPedido;
    private List<DetallePedidoDTO> detalles;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetallePedidoDTO {
        private Long id;
        private Long productoId;
        private String sku;
        private String nombreProducto;
        private Integer precioUnitario;
        private Integer cantidad;
        private Integer subtotal;
    }
}