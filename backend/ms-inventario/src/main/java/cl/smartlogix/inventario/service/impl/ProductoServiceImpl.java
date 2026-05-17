package cl.smartlogix.inventario.service.impl;

import cl.smartlogix.inventario.dto.request.ProductoRequestDTO;
import cl.smartlogix.inventario.dto.response.ProductoResponseDTO;
import cl.smartlogix.inventario.entity.Categoria;
import cl.smartlogix.inventario.entity.Producto;
import cl.smartlogix.inventario.exception.DuplicateResourceException;
import cl.smartlogix.inventario.exception.ResourceNotFoundException;
import cl.smartlogix.inventario.mapper.ProductoMapper;
import cl.smartlogix.inventario.repository.CategoriaRepository;
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
    private final CategoriaRepository categoriaRepository;

    // Crear un nuevo producto con validación de SKU único
    @Override
    @Transactional
    public ProductoResponseDTO createProducto(ProductoRequestDTO request) {
        log.debug("Procesando creación del producto: {}", request.getNombre());
        if (request.getSku() != null && productoRepository.existsBySku(request.getSku())) {
            throw new DuplicateResourceException("El SKU '" + request.getSku() + "' ya está registrado");
        }
        Producto producto = productoMapper.toEntity(request);
        Categoria categoriaReal = categoriaRepository.findById(request.getCategoriaId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Categoría no encontrada con ID: " + request.getCategoriaId()));
        producto.setCategoria(categoriaReal);
        Producto productoGuardado = productoRepository.save(producto);
        log.info("Producto creado exitosamente con ID: {}", productoGuardado.getId());
        return productoMapper.toResponseDTO(productoGuardado);
    }

    // Actualizar un producto existente con validación de SKU único y manejo de
    // excepciones
    @Override
    @Transactional
    public ProductoResponseDTO updateProducto(Long id, ProductoRequestDTO request) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        if (!producto.getSku().equals(request.getSku()) && productoRepository.existsBySku(request.getSku())) {
            throw new DuplicateResourceException("El SKU ya está registrado en otro producto");
        }
        productoMapper.updateEntity(producto, request);
        Categoria categoriaReal = categoriaRepository.findById(request.getCategoriaId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Categoría no encontrada con ID: " + request.getCategoriaId()));
        producto.setCategoria(categoriaReal);
        Producto productoGuardado = productoRepository.save(producto);
        return productoMapper.toResponseDTO(productoGuardado);
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
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
    public ProductoResponseDTO getProductoBySlug(String slug) {
        Producto producto = productoRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con slug: " + slug));
        return productoMapper.toResponseDTO(producto);
    }

    // Método para buscar por SKU
    @Override
    @Transactional(readOnly = true)
    public ProductoResponseDTO getProductoBySku(String sku) {
        Producto producto = productoRepository.findBySku(sku)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con sku: " + sku));
        return productoMapper.toResponseDTO(producto);
    }

    // Obtener productos por categoría (incluyendo subcategorías directas)
    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> getProductosByCategoria(Long categoriaId) {
        List<Producto> productos = productoRepository.findByCategoriaId(categoriaId);
        // Si usas un list mapping en MapStruct:
        return productoMapper.toResponseDTOList(productos);
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