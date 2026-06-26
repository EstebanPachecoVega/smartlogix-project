package cl.smartlogix.pedidos.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "detalles_pedido", indexes = {
    @Index(name = "idx_detalle_pedido_id", columnList = "pedido_id"),
    @Index(name = "idx_detalle_producto_id", columnList = "producto_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetallePedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "producto_id", nullable = false)
    private Long productoId;

    @Column(nullable = false)
    private String sku;

    @Column(name = "nombre_producto", nullable = false)
    private String nombreProducto;

    @Column(name = "precio_unitario", nullable = false)
    private Integer precioUnitario;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(nullable = false)
    private Integer subtotal;

    @Column(name = "imagen_principal")
    private String imagenPrincipal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id")
    private Pedido pedido;
}