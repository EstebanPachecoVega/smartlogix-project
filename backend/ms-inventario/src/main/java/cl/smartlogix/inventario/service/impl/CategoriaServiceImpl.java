package cl.smartlogix.inventario.service.impl;

import cl.smartlogix.inventario.dto.request.CategoriaRequestDTO;
import cl.smartlogix.inventario.dto.response.CategoriaResponseDTO;
import cl.smartlogix.inventario.entity.Categoria;
import cl.smartlogix.inventario.exception.DomainException;
import cl.smartlogix.inventario.exception.DuplicateResourceException;
import cl.smartlogix.inventario.exception.ResourceNotFoundException;
import cl.smartlogix.inventario.mapper.CategoriaMapper;
import cl.smartlogix.inventario.repository.CategoriaRepository;
import cl.smartlogix.inventario.repository.ProductoRepository;
import cl.smartlogix.inventario.service.CategoriaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;
    private final CategoriaMapper categoriaMapper;

    // Crear una nueva categoría con validaciones de unicidad y existencia de padre
    @Override
    @Transactional
    public CategoriaResponseDTO createCategoria(CategoriaRequestDTO request) {
        log.debug("Procesando creación de categoría: {}", request.getNombre());

        if (categoriaRepository.existsByNombre(request.getNombre())) {
            throw new DuplicateResourceException(
                    "Ya existe una categoría registrada con el nombre: " + request.getNombre());
        }
        if (categoriaRepository.existsBySlug(request.getSlug())) {
            throw new DuplicateResourceException(
                    "Ya existe una categoría registrada con el slug: " + request.getSlug());
        }

        if (request.getPadreId() != null && !categoriaRepository.existsById(request.getPadreId())) {
            throw new ResourceNotFoundException("La categoría padre con ID " + request.getPadreId() + " no existe.");
        }

        Categoria categoria = categoriaMapper.toEntity(request);
        Categoria saved = categoriaRepository.save(categoria);
        return categoriaMapper.toResponseDTO(saved);
    }

    // Validación de integridad y consistencia antes de actualizar una categoría
    @Override
    @Transactional
    public CategoriaResponseDTO updateCategoria(Long id, CategoriaRequestDTO request) {
        log.debug("Procesando actualización de categoría ID: {}", id);

        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + id));

        // Validación de consistencia jerárquica
        if (id.equals(request.getPadreId())) {
            throw new DomainException(
                    "Error de consistencia: Una categoría no puede ser asignada como su propia categoría padre.");
        }

        if (categoriaRepository.existsByNombreAndIdNot(request.getNombre(), id)) {
            throw new DuplicateResourceException(
                    "El nombre '" + request.getNombre() + "' ya está siendo usado por otra categoría.");
        }
        if (categoriaRepository.existsBySlugAndIdNot(request.getSlug(), id)) {
            throw new DuplicateResourceException(
                    "El slug '" + request.getSlug() + "' ya está siendo usado por otra categoría.");
        }

        if (request.getPadreId() != null && !categoriaRepository.existsById(request.getPadreId())) {
            throw new ResourceNotFoundException("La categoría padre con ID " + request.getPadreId() + " no existe.");
        }

        categoriaMapper.updateEntity(categoria, request);
        Categoria updated = categoriaRepository.save(categoria);
        return categoriaMapper.toResponseDTO(updated);
    }

    // Validación de integridad referencial antes de eliminar una categoría
    @Override
    @Transactional
    public void deleteCategoria(Long id) {
        log.debug("Procesando eliminación de categoría ID: {}", id);
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + id));
        if (!productoRepository.buscarPorCategoriaOPadre(id, id).isEmpty()) {
            throw new DomainException(
                    "No se puede eliminar la categoría porque tiene productos activos asociados en ella o en sus subcategorías directas.");
        }
        categoriaRepository.delete(categoria);
        log.info("Categoría ID {} eliminada exitosamente del sistema", id);
    }

    // Consulta por ID con manejo de excepciones para casos no encontrados
    @Override
    @Transactional(readOnly = true)
    public CategoriaResponseDTO getCategoriaById(Long id) {
        return categoriaRepository.findById(id)
                .map(categoriaMapper::toResponseDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + id));
    }

    // Consulta por slug con manejo de excepciones para casos no encontrados
    @Override
    @Transactional(readOnly = true)
    public CategoriaResponseDTO getCategoriaBySlug(String slug) {
        return categoriaRepository.findBySlug(slug)
                .map(categoriaMapper::toResponseDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con el slug: " + slug));
    }

    // Consulta de todas las categorías con mapeo a DTOs para una respuesta estructurada
    @Override
    @Transactional(readOnly = true)
    public List<CategoriaResponseDTO> getAllCategorias() {
        return categoriaRepository.findAll().stream()
                .map(categoriaMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    // Consulta de categorías principales (sin padre) con mapeo a DTOs para una respuesta estructurada
    @Override
    @Transactional(readOnly = true)
    public List<CategoriaResponseDTO> getCategoriasPrincipales() {
        return categoriaRepository.findByPadreIsNull().stream()
                .map(categoriaMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
}