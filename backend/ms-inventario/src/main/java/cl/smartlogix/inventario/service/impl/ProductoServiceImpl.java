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
import cl.smartlogix.inventario.service.RedisStockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final RedisStockService redisStockService;

    @Override
    @Transactional
    public ProductoResponseDTO createProducto(ProductoRequestDTO request) {
        log.debug("Procesando creación del producto: {}", request.getNombre());
        if (request.getSku() != null && productoRepository.existsBySku(request.getSku())) {
            throw new DuplicateResourceException("El SKU '" + request.getSku() + "' ya está registrado");
        }
        Producto producto = productoMapper.toEntity(request);
        Categoria categoriaReal = categoriaRepository.findById(request.getCategoriaId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + request.getCategoriaId()));
        producto.setCategoria(categoriaReal);
        Producto productoGuardado = productoRepository.save(producto);
        
        // Sincronizar en Redis
        redisStockService.inicializarStock(productoGuardado.getId(), productoGuardado.getCantidad());
        log.info("Producto creado y sincronizado en Redis con ID: {}", productoGuardado.getId());
        
        return productoMapper.toResponseDTO(productoGuardado);
    }

    @Override
    @Transactional
    public ProductoResponseDTO updateProducto(Long id, ProductoRequestDTO request) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));

        if (!producto.getSku().equals(request.getSku()) && productoRepository.existsBySku(request.getSku())) {
            throw new DuplicateResourceException("El SKU ya está registrado en otro producto");
        }
        productoMapper.updateEntity(producto, request);
        Categoria categoriaReal = categoriaRepository.findById(request.getCategoriaId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + request.getCategoriaId()));
        producto.setCategoria(categoriaReal);
        Producto productoGuardado = productoRepository.save(producto);
        
        // Actualizar Redis con el nuevo stock
        redisStockService.inicializarStock(productoGuardado.getId(), productoGuardado.getCantidad());
        log.info("Producto actualizado y sincronizado en Redis con ID: {}", productoGuardado.getId());
        
        return productoMapper.toResponseDTO(productoGuardado);
    }

    @Override
    @Transactional
    public void deleteProducto(Long id) {
        log.debug("Eliminando producto id: {}", id);
        if (!productoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Producto no encontrado con id: " + id);
        }
        productoRepository.deleteById(id);
        // Eliminar de Redis
        redisStockService.eliminarStock(id);
        log.info("Producto {} eliminado de la base de datos y de Redis", id);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoResponseDTO getProductoById(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id: " + id));
        return productoMapper.toResponseDTO(producto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> getAllProductos() {
        return productoRepository.findAll().stream()
                .map(productoMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoResponseDTO getProductoBySlug(String slug) {
        Producto producto = productoRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con slug: " + slug));
        return productoMapper.toResponseDTO(producto);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoResponseDTO getProductoBySku(String sku) {
        Producto producto = productoRepository.findBySku(sku)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con sku: " + sku));
        return productoMapper.toResponseDTO(producto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> getProductosByCategoria(Long categoriaId) {
        List<Producto> productos = productoRepository.findByCategoriaId(categoriaId);
        return productoMapper.toResponseDTOList(productos);
    }

    @Override
    public List<ProductoResponseDTO> getProductosFiltrados(String nombre, Long categoriaId, Boolean conStock, Integer precioMin, Integer precioMax, Boolean destacado, Boolean novedad) {
        log.debug("Filtrando productos por criterios avanzados");
        String nombreFiltro = (nombre != null && !nombre.isBlank()) ? nombre : null;
        return productoRepository.filtrarProductos(nombreFiltro, categoriaId, conStock, precioMin, precioMax, destacado, novedad)
                .stream()
                .map(productoMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
}