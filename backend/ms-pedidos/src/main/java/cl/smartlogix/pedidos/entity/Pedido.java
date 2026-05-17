package cl.smartlogix.pedidos.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos", indexes = {
    @Index(name = "idx_pedido_numero_orden", columnList = "numero_orden", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_orden", nullable = false, unique = true)
    private String numeroOrden;

    @Column(name = "fecha_pedido", nullable = false)
    private LocalDateTime fechaPedido;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPedido estado;

    @Column(name = "total_compra", nullable = false)
    private Integer totalCompra;

    // --- DATOS DE AUDITORÍA Y LOGÍSTICA ---
    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;
    
    private String destinatario;
    private String calle;
    private String numero;
    private String comuna;
    private String ciudad;
    @Column(name = "codigo_postal")
    private String codigoPostal;
    @Column(name = "metodo_envio")
    private String metodoEnvio;
    @Column(name = "peso_kg")
    private Double pesoKg;
    private String dimensiones;
    // ---------------------------------------------

    @Builder.Default
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetallePedido> detalles = new ArrayList<>();

    public void agregarDetalle(DetallePedido detalle) {
        detalles.add(detalle);
        detalle.setPedido(this);
    }
}