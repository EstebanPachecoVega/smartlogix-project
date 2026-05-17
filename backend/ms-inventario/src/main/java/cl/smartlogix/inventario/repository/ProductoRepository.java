package cl.smartlogix.inventario.repository;

import cl.smartlogix.inventario.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

        Optional<Producto> findBySku(String sku);

        Optional<Producto> findBySlug(String slug);

        boolean existsBySku(String sku);

        @Modifying
        @Query("UPDATE Producto p SET p.cantidad = p.cantidad - :cantidad WHERE p.id = :productoId AND p.cantidad >= :cantidad")
        int restarStockAtomico(@Param("productoId") Long productoId, @Param("cantidad") Integer cantidad);

        @Query("SELECT p FROM Producto p WHERE p.categoria.id = :categoriaId OR p.categoria.padre.id = :padreId")
        List<Producto> buscarPorCategoriaOPadre(@Param("categoriaId") Long categoriaId, @Param("padreId") Long padreId);

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