package cl.smartlogix.inventario.repository;

import cl.smartlogix.inventario.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

        Optional<Producto> findBySku(String sku);

        Optional<Producto> findBySlug(String slug);

        boolean existsBySku(String sku);

        List<Producto> findByNombreContainingIgnoreCase(String nombre);

        List<Producto> findByCategoriaIdOrCategoriaPadreId(Long categoriaId, Long padreId);

        // 🔍 Filtrador avanzado dinámico
        @Query("SELECT p FROM Producto p WHERE " +
                        "(:nombre IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) AND " +
                        "(:categoriaId IS NULL OR p.categoria.id = :categoriaId) AND " +
                        "(:conStock IS NULL OR " +
                        "  (:conStock = true AND p.cantidad > 0) OR " +
                        "  (:conStock = false AND p.cantidad = 0)) AND " +
                        "(:precioMin IS NULL OR p.precio >= :precioMin) AND " +
                        "(:precioMax IS NULL OR p.precio <= :precioMax)")
        List<Producto> filtrarProductos(
                        @Param("nombre") String nombre,
                        @Param("categoriaId") Long categoriaId,
                        @Param("conStock") Boolean conStock,
                        @Param("precioMin") Integer precioMin,
                        @Param("precioMax") Integer precioMax);
}