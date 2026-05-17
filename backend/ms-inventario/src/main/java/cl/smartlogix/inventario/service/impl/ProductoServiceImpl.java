package cl.smartlogix.inventario.service.impl;

import cl.smartlogix.inventario.dto.request.ProductoRequestDTO;
import cl.smartlogix.inventario.dto.response.ProductoResponseDTO;
import cl.smartlogix.inventario.entity.Producto;
import cl.smartlogix.inventario.exception.DuplicateResourceException;
import cl.smartlogix.inventario.exception.ResourceNotFoundException;
import cl.smartlogix.inventario.mapper.ProductoMapper;
import cl.smartlogix.inventario.repository.ProductoRepository;
import cl.smartlogix.inventario.service.ProductoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final ProductoMapper productoMapper;

    // Crear un nuevo producto con validación de SKU único
    @Override
    @Transactional
    public ProductoResponseDTO createProducto(ProductoRequestDTO request) {
        log.debug("Creando producto: {}", request.getNombre());
        if (request.getSku() != null && productoRepository.existsBySku(request.getSku())) {
            throw new DuplicateResourceException("Ya existe un producto con el mismo SKU: " + request.getSku());
        }
        try {
            Producto producto = productoMapper.toEntity(request);
            Producto saved = productoRepository.save(producto);
            return productoMapper.toResponseDTO(saved);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateResourceException("Ya existe un producto con el mismo nombre o slug");
        }
    }

    // Actualizar un producto por ID con validación de SKU único
    @Override
    @Transactional
    public ProductoResponseDTO updateProducto(Long id, ProductoRequestDTO request) {
        log.debug("Actualizando producto id: {}", id);
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id: " + id));
        if (request.getSku() != null && !request.getSku().equals(producto.getSku())) {
            if (productoRepository.existsBySku(request.getSku())) {
                throw new DuplicateResourceException("No puedes usar ese SKU porque ya pertenece a otro producto");
            }
        }
        productoMapper.updateEntity(producto, request);
        try {
            Producto updated = productoRepository.save(producto);
            return productoMapper.toResponseDTO(updated);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateResourceException("Conflicto: nombre o slug ya existe");
        }
    }

    // Eliminar un producto por ID con manejo de excepciónes
    @Override
    @Transactional
    public void deleteProducto(Long id) {
        log.debug("Eliminando producto id: {}", id);
        if (!productoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Producto no encontrado con id: " + id);
        }
        productoRepository.deleteById(id);
    }

    // Obtener un producto por ID con manejo de excepciones
    @Override
    public ProductoResponseDTO getProductoById(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id: " + id));
        return productoMapper.toResponseDTO(producto);
    }

    // Obtener todos los productos con manejo de excepciones
    @Override
    public List<ProductoResponseDTO> getAllProductos() {
        return productoRepository.findAll().stream()
                .map(productoMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    // Método para buscar por slug
    @Override
    public ProductoResponseDTO getProductoBySlug(String slug) {
        Producto producto = productoRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con slug: " + slug));
        return productoMapper.toResponseDTO(producto);
    }

    // Método para buscar por SKU
    @Override
    public ProductoResponseDTO getProductoBySku(String sku) {
        Producto producto = productoRepository.findBySku(sku)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con SKU: " + sku));
        return productoMapper.toResponseDTO(producto);
    }

    // Obtener productos por categoría (incluyendo subcategorías directas)
    @Override
    public List<ProductoResponseDTO> getProductosPorCategoria(Long categoriaId) {
        log.debug("Obteniendo productos de la categoría y sus subcategorías directas para el ID: {}", categoriaId);
        return productoRepository.findByCategoriaIdOrCategoriaPadreId(categoriaId, categoriaId)
                .stream()
                .map(productoMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    // Filtrar productos por múltiples criterios de forma avanzada y consistente
    @Override
    public List<ProductoResponseDTO> getProductosFiltrados(
            String nombre, Long categoriaId, Boolean conStock, Integer precioMin, Integer precioMax) {

        log.debug("Filtrando productos por criterios avanzados y consistentes");
        String nombreFiltro = (nombre != null && !nombre.isBlank()) ? nombre : null;

        return productoRepository.filtrarProductos(nombreFiltro, categoriaId, conStock, precioMin, precioMax)
                .stream()
                .map(productoMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
}