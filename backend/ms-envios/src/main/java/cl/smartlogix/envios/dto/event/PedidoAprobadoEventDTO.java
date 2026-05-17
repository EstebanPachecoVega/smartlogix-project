package cl.smartlogix.envios.dto.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PedidoAprobadoEventDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long pedidoId;
    private Long usuarioId;
    private String destinatario;
    private String calle;
    private String numero;
    private String comuna;
    private String ciudad;
    private String codigoPostal;
    private String metodoEnvio;
    private Double pesoKg;
    private String dimensiones;
}