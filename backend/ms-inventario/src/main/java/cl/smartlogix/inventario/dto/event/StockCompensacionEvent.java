package cl.smartlogix.inventario.dto.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockCompensacionEvent {
    private Long pedidoId;
    private String numeroOrden;
    private Long productoId;
    private Integer cantidad;
}