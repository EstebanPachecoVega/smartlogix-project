package cl.smartlogix.pedidos.dto.event;

import lombok.*;
import java.io.Serializable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PedidoAprobadoEventDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long pedidoId;
    private String numeroOrden;
    
    // --- DATOS PARA EL MS-ENVIOS ---
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