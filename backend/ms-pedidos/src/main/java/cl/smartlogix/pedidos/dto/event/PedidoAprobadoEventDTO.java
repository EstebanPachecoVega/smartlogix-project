package cl.smartlogix.pedidos.dto.event;

import lombok.*;
import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PedidoAprobadoEventDTO implements Serializable {
    private Long pedidoId;
    private String numeroOrden;
    private List<ItemEventDTO> items;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemEventDTO implements Serializable {
        private Long productoId;
        private Integer cantidad;
    }
}