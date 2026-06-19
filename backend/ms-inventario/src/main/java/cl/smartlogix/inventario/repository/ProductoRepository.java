package cl.smartlogix.inventario.repository;

import cl.smartlogix.inventario.entity.Producto;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

        @EntityGraph(attributePaths = { "categoria" })
        Optional<Producto> findById(Long id);

        @EntityGraph(attributePaths = { "categoria" })
        Optional<Producto> findBySlug(String slug);

        @EntityGraph(attributePaths = { "categoria" })
        Optional<Producto> findBySku(String sku);

        @EntityGraph(attributePaths = { "categoria" })
        List<Producto> findByCategoriaId(Long categoriaId);

        @EntityGraph(attributePaths = { "categoria" })
        List<Producto> findByCategoriaIdIn(List<Long> categoriaIds);

        boolean existsBySku(String sku);

        boolean existsByNombre(String nombre);

        boolean existsBySlug(String slug);

        boolean existsByCategoriaIdIn(List<Long> categoriaIds);

        @Modifying
        @Query("UPDATE Producto p SET p.cantidad = p.cantidad - :cantidad WHERE p.id = :productoId AND p.cantidad >= :cantidad")
        int restarStockAtomico(@Param("productoId") Long productoId, @Param("cantidad") Integer cantidad);

        @Modifying
        @Query("UPDATE Producto p SET p.cantidad = p.cantidad + :cantidad WHERE p.id = :id")
        int adicionarStockAtomico(@Param("id") Long id, @Param("cantidad") Integer cantidad);

        @Query("SELECT p FROM Producto p WHERE p.categoria.id = :categoriaId OR p.categoria.padre.id = :padreId")
        List<Producto> buscarPorCategoriaOPadre(@Param("categoriaId") Long categoriaId, @Param("padreId") Long padreId);

        @Query("SELECT p FROM Producto p JOIN FETCH p.categoria")
        List<Producto> findAll();

        @Query("SELECT p FROM Producto p JOIN FETCH p.categoria WHERE " +
                        "(:nombre IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) AND " +
                        "(:categoriaId IS NULL OR p.categoria.id = :categoriaId) AND " +
                        "(:conStock IS NULL OR " +
                        "  (:conStock = true AND p.cantidad > 0) OR " +
                        "  (:conStock = false AND p.cantidad = 0)) AND " +
                        "(:precioMin IS NULL OR p.precio >= :precioMin) AND " +
                        "(:precioMax IS NULL OR p.precio <= :precioMax) AND " +
                        "(:destacado IS NULL OR p.destacado = :destacado) AND " +
                        "(:novedad IS NULL OR p.novedad = :novedad)")
        List<Producto> filtrarProductos(
                        @Param("nombre") String nombre,
                        @Param("categoriaId") Long categoriaId,
                        @Param("conStock") Boolean conStock,
                        @Param("precioMin") Integer precioMin,
                        @Param("precioMax") Integer precioMax,
                        @Param("destacado") Boolean destacado,
                        @Param("novedad") Boolean novedad);
}