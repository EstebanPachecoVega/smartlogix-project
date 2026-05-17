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

import java.util.ArrayList;
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

    // Actualizar una categoría existente con validaciones de unicidad, existencia y
    // ciclos jerárquicos
    @Override
    @Transactional
    public CategoriaResponseDTO updateCategoria(Long id, CategoriaRequestDTO request) {
        log.debug("Actualizando categoría ID: {}", id);
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + id));
        if (!categoria.getNombre().equals(request.getNombre())
                && categoriaRepository.existsByNombreAndIdNot(request.getNombre(), id)) {
            throw new DuplicateResourceException("El nombre de la categoría ya existe");
        }
        if (!categoria.getSlug().equals(request.getSlug())
                && categoriaRepository.existsBySlugAndIdNot(request.getSlug(), id)) {
            throw new DuplicateResourceException("El slug de la categoría ya existe");
        }
        Categoria nuevoPadre = null;
        if (request.getPadreId() != null) {
            nuevoPadre = categoriaRepository.findById(request.getPadreId())
                    .orElseThrow(() -> new ResourceNotFoundException("La categoría padre indicada no existe"));
            validarCicloJerarquico(id, nuevoPadre);
        }
        categoriaMapper.updateEntity(categoria, request);
        categoria.setPadre(nuevoPadre);
        Categoria categoriaGuardada = categoriaRepository.save(categoria);
        return categoriaMapper.toResponseDTO(categoriaGuardada);
    }

    // Validar y eliminar una categoría solo si no tiene productos asociados en toda
    // su rama de subcategorías
    @Override
    @Transactional
    public void deleteCategoria(Long id) {
        log.debug("Evaluando eliminación de categoría ID: {}", id);
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + id));
        List<Long> idsRamaCompleta = obtenerIdsRamaCompleta(categoria);
        boolean existenProductos = productoRepository.existsByCategoriaIdIn(idsRamaCompleta);
        if (existenProductos) {
            throw new DomainException(
                    "No se puede eliminar la categoría porque contiene productos (directamente o en sus subcategorías).");
        }
        categoriaRepository.delete(categoria);
        log.info("Categoría ID: {} eliminada exitosamente junto con subcategorías vacías", id);
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

    // Consulta de todas las categorías con mapeo a DTOs para una respuesta
    // estructurada
    @Override
    @Transactional(readOnly = true)
    public List<CategoriaResponseDTO> getAllCategorias() {
        return categoriaRepository.findAll().stream()
                .map(categoriaMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    // Consulta de categorías principales (sin padre) con mapeo a DTOs para una
    // respuesta estructurada
    @Override
    @Transactional(readOnly = true)
    public List<CategoriaResponseDTO> getCategoriasPrincipales() {
        return categoriaRepository.findByPadreIsNull().stream()
                .map(categoriaMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    // Validación de ciclos jerárquicos para evitar inconsistencias en la estructura
    // de categorías y garantizar la integridad de los datos
    private void validarCicloJerarquico(Long idCategoriaActual, Categoria nuevoPadre) {
        if (nuevoPadre == null)
            return;
        if (idCategoriaActual.equals(nuevoPadre.getId())) {
            throw new DomainException("Una categoría no puede ser padre de sí misma");
        }
        Categoria ancestro = nuevoPadre.getPadre();
        while (ancestro != null) {
            if (idCategoriaActual.equals(ancestro.getId())) {
                throw new DomainException(
                        "Error jerárquico: No puedes asignar como padre a una categoría que ya es descendiente de esta (Ciclo detectado)");
            }
            ancestro = ancestro.getPadre();
        }
    }

    // Método auxiliar para obtener todos los IDs de una categoría y sus
    // subcategorías de forma recursiva
    private List<Long> obtenerIdsRamaCompleta(Categoria categoria) {
        List<Long> ids = new ArrayList<>();
        ids.add(categoria.getId());

        if (categoria.getSubcategorias() != null) {
            for (Categoria sub : categoria.getSubcategorias()) {
                ids.addAll(obtenerIdsRamaCompleta(sub));
            }
        }
        return ids;
    }
}