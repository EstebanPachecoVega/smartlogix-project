package cl.smartlogix.pedidos.dto.event;

import lombok.*;
import java.io.Serializable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PedidoRechazadoEventDTO implements Serializable {
    private Long pedidoId;
    private String numeroOrden;
    private Long productoId;
    private Integer cantidad;
}