package cl.smartlogix.envios.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "envios")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Envio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pedido_id", nullable = false)
    private Long pedidoId;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(nullable = false, length = 150)
    private String destinatario;

    @Column(nullable = false, length = 150)
    private String calle;

    @Column(nullable = false, length = 20)
    private String numero;

    @Column(nullable = false, length = 100)
    private String comuna;

    @Column(nullable = false, length = 100)
    private String ciudad;

    @Column(name = "codigo_postal", length = 20)
    private String codigoPostal;

    @Column(name = "metodo_envio", nullable = false, length = 50)
    private String metodoEnvio;

    @Column(name = "empresa_logistica", length = 100)
    private String empresaLogistica;

    @Column(name = "numero_tracking", length = 50, unique = true)
    private String numeroTracking;

    @Column(name = "fecha_estimada_entrega")
    private LocalDate fechaEstimadaEntrega;

    @Column(name = "estado_envio", nullable = false, length = 30)
    private String estadoEnvio;

    @Column(name = "peso_kg")
    private Double pesoKg;

    @Column(length = 50)
    private String dimensiones;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    /** Método para asegurar que cada envío tenga una fecha de creación y un estado inicial definido. **/
    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
        if (this.estadoEnvio == null) {
            this.estadoEnvio = "CREADO";
        }
    }
}