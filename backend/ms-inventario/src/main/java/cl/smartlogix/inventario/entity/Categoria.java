package cl.smartlogix.inventario.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categorias", indexes = {
    @Index(name = "idx_nombre", columnList = "nombre"),
    @Index(name = "idx_slug", columnList = "slug"),
    @Index(name = "idx_padre_id", columnList = "id_padre")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(nullable = false, unique = true, length = 120)
    private String slug;

    @Column(length = 500)
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_padre")
    private Categoria padre;

    @Builder.Default
    @OneToMany(mappedBy = "padre", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Categoria> subcategorias = new ArrayList<>();

    @Column(name = "orden_visual")
    private Integer ordenVisual;

    @Column(name = "activo")
    @Builder.Default
    private Boolean activo = true;

    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    /**
     * Agrega una subcategoría y automáticamente le asigna este como padre.
     */
    public void addSubcategoria(Categoria subcategoria) {
        this.subcategorias.add(subcategoria);
        subcategoria.setPadre(this);
    }

    /**
     * Remueve una subcategoría y automáticamente le quita el padre.
     */
    public void removeSubcategoria(Categoria subcategoria) {
        this.subcategorias.remove(subcategoria);
        subcategoria.setPadre(null);
    }
}