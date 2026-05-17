package cl.smartlogix.inventario.repository;

import cl.smartlogix.inventario.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    
    Optional<Categoria> findBySlug(String slug);
    
    boolean existsByNombre(String nombre);
    
    boolean existsBySlug(String slug);
    
    boolean existsByNombreAndIdNot(String nombre, Long id);
    
    boolean existsBySlugAndIdNot(String slug, Long id);
    
    List<Categoria> findByPadreIsNull();
}