package cl.smartlogix.pedidos.dto.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PedidoRechazadoEventDTO implements Serializable {
    private Long pedidoId;
    private Long productoId;
    private Integer cantidad;
}