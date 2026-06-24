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
import cl.smartlogix.inventario.service.RedisStockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoServiceImplTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private ProductoMapper productoMapper;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private RedisStockService redisStockService;

    private ProductoServiceImpl productoService;

    @BeforeEach
    void setUp() {
        productoService = new ProductoServiceImpl(productoRepository, productoMapper, categoriaRepository, redisStockService);
    }

    @Test
    void createProducto_ok() {
        ProductoRequestDTO request = new ProductoRequestDTO();
        request.setSku("SKU-001");
        request.setNombre("Producto Test");
        request.setSlug("producto-test");
        request.setCategoriaId(1L);

        when(productoRepository.existsBySku("SKU-001")).thenReturn(false);
        when(productoRepository.existsByNombre("Producto Test")).thenReturn(false);
        when(productoRepository.existsBySlug("producto-test")).thenReturn(false);

        Categoria categoria = new Categoria();
        categoria.setId(1L);
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));

        Producto producto = new Producto();
        producto.setId(10L);
        producto.setCantidad(50);
        when(productoMapper.toEntity(request)).thenReturn(producto);
        when(productoRepository.save(producto)).thenReturn(producto);

        ProductoResponseDTO expectedDto = new ProductoResponseDTO();
        when(productoMapper.toResponseDTO(producto)).thenReturn(expectedDto);

        ProductoResponseDTO result = productoService.createProducto(request);

        assertThat(result).isEqualTo(expectedDto);
        verify(redisStockService).inicializarStock(10L, 50);
    }

    @Test
    void createProducto_skuDuplicado_lanzaDuplicateResourceException() {
        ProductoRequestDTO request = new ProductoRequestDTO();
        request.setSku("SKU-001");
        request.setNombre("Producto Test");
        request.setSlug("producto-test");

        when(productoRepository.existsBySku("SKU-001")).thenReturn(true);

        assertThatThrownBy(() -> productoService.createProducto(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("SKU");
    }

    @Test
    void createProducto_nombreDuplicado_lanzaDuplicateResourceException() {
        ProductoRequestDTO request = new ProductoRequestDTO();
        request.setSku("SKU-001");
        request.setNombre("Producto Test");
        request.setSlug("producto-test");

        when(productoRepository.existsBySku("SKU-001")).thenReturn(false);
        when(productoRepository.existsByNombre("Producto Test")).thenReturn(true);

        assertThatThrownBy(() -> productoService.createProducto(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("nombre");
    }

    @Test
    void createProducto_slugDuplicado_lanzaDuplicateResourceException() {
        ProductoRequestDTO request = new ProductoRequestDTO();
        request.setSku("SKU-001");
        request.setNombre("Producto Test");
        request.setSlug("producto-test");

        when(productoRepository.existsBySku("SKU-001")).thenReturn(false);
        when(productoRepository.existsByNombre("Producto Test")).thenReturn(false);
        when(productoRepository.existsBySlug("producto-test")).thenReturn(true);

        assertThatThrownBy(() -> productoService.createProducto(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("slug");
    }

    @Test
    void createProducto_skuNull_ok() {
        ProductoRequestDTO request = new ProductoRequestDTO();
        request.setNombre("Producto Test");
        request.setSlug("producto-test");
        request.setCategoriaId(1L);

        when(productoRepository.existsByNombre("Producto Test")).thenReturn(false);
        when(productoRepository.existsBySlug("producto-test")).thenReturn(false);

        Categoria categoria = new Categoria();
        categoria.setId(1L);
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));

        Producto producto = new Producto();
        producto.setId(10L);
        producto.setCantidad(50);
        when(productoMapper.toEntity(request)).thenReturn(producto);
        when(productoRepository.save(producto)).thenReturn(producto);

        ProductoResponseDTO expectedDto = new ProductoResponseDTO();
        when(productoMapper.toResponseDTO(producto)).thenReturn(expectedDto);

        productoService.createProducto(request);

        verify(redisStockService).inicializarStock(10L, 50);
    }

    @Test
    void createProducto_categoriaNoExiste_lanzaResourceNotFoundException() {
        ProductoRequestDTO request = new ProductoRequestDTO();
        request.setSku("SKU-001");
        request.setNombre("Producto Test");
        request.setSlug("producto-test");
        request.setCategoriaId(999L);

        when(productoRepository.existsBySku("SKU-001")).thenReturn(false);
        when(productoRepository.existsByNombre("Producto Test")).thenReturn(false);
        when(productoRepository.existsBySlug("producto-test")).thenReturn(false);
        when(categoriaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productoService.createProducto(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Categoría");
    }

    @Test
    void updateProducto_ok() {
        ProductoRequestDTO request = new ProductoRequestDTO();
        request.setSku("SKU-002");
        request.setNombre("Producto Actualizado");
        request.setSlug("producto-actualizado");

        Producto productoExistente = new Producto();
        productoExistente.setId(1L);
        productoExistente.setSku("SKU-001");
        productoExistente.setNombre("Producto Original");
        productoExistente.setSlug("producto-original");
        productoExistente.setCantidad(30);

        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoExistente));
        when(productoRepository.existsBySku("SKU-002")).thenReturn(false);
        when(productoRepository.existsByNombre("Producto Actualizado")).thenReturn(false);
        when(productoRepository.existsBySlug("producto-actualizado")).thenReturn(false);
        when(productoRepository.save(productoExistente)).thenReturn(productoExistente);

        ProductoResponseDTO expectedDto = new ProductoResponseDTO();
        when(productoMapper.toResponseDTO(productoExistente)).thenReturn(expectedDto);

        ProductoResponseDTO result = productoService.updateProducto(1L, request);

        assertThat(result).isEqualTo(expectedDto);
        verify(redisStockService).inicializarStock(1L, 30);
    }

    @Test
    void updateProducto_conCategoria_ok() {
        ProductoRequestDTO request = new ProductoRequestDTO();
        request.setSku("SKU-002");
        request.setNombre("Producto Actualizado");
        request.setSlug("producto-actualizado");
        request.setCategoriaId(1L);

        Producto productoExistente = new Producto();
        productoExistente.setId(1L);
        productoExistente.setSku("SKU-001");
        productoExistente.setNombre("Producto Original");
        productoExistente.setSlug("producto-original");
        productoExistente.setCantidad(30);

        Categoria categoria = new Categoria();
        categoria.setId(1L);
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoExistente));
        when(productoRepository.existsBySku("SKU-002")).thenReturn(false);
        when(productoRepository.existsByNombre("Producto Actualizado")).thenReturn(false);
        when(productoRepository.existsBySlug("producto-actualizado")).thenReturn(false);
        when(productoRepository.save(productoExistente)).thenReturn(productoExistente);

        ProductoResponseDTO expectedDto = new ProductoResponseDTO();
        when(productoMapper.toResponseDTO(productoExistente)).thenReturn(expectedDto);

        ProductoResponseDTO result = productoService.updateProducto(1L, request);

        assertThat(result).isEqualTo(expectedDto);
        verify(redisStockService).inicializarStock(1L, 30);
    }

    @Test
    void updateProducto_noExiste_lanzaResourceNotFoundException() {
        when(productoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productoService.updateProducto(999L, new ProductoRequestDTO()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Producto no encontrado");
    }

    @Test
    void deleteProducto_ok() {
        when(productoRepository.existsById(1L)).thenReturn(true);

        productoService.deleteProducto(1L);

        verify(productoRepository).deleteById(1L);
        verify(redisStockService).eliminarStock(1L);
    }

    @Test
    void deleteProducto_noExiste_lanzaResourceNotFoundException() {
        when(productoRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> productoService.deleteProducto(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Producto no encontrado");
    }

    @Test
    void getProductoById_ok() {
        Producto producto = new Producto();
        producto.setId(1L);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        ProductoResponseDTO expectedDto = new ProductoResponseDTO();
        when(productoMapper.toResponseDTO(producto)).thenReturn(expectedDto);

        ProductoResponseDTO result = productoService.getProductoById(1L);

        assertThat(result).isEqualTo(expectedDto);
    }

    @Test
    void getProductoById_noExiste_lanzaResourceNotFoundException() {
        when(productoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productoService.getProductoById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getAllProductos_retornaLista() {
        Producto p1 = new Producto();
        Producto p2 = new Producto();
        when(productoRepository.findAll()).thenReturn(List.of(p1, p2));
        when(productoMapper.toResponseDTO(p1)).thenReturn(new ProductoResponseDTO());
        when(productoMapper.toResponseDTO(p2)).thenReturn(new ProductoResponseDTO());

        List<ProductoResponseDTO> result = productoService.getAllProductos();

        assertThat(result).hasSize(2);
    }

    @Test
    void getProductoBySlug_noExiste_lanzaResourceNotFoundException() {
        when(productoRepository.findBySlug("no-existe")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productoService.getProductoBySlug("no-existe"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getProductoBySku_noExiste_lanzaResourceNotFoundException() {
        when(productoRepository.findBySku("NO-SKU")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productoService.getProductoBySku("NO-SKU"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getProductosByCategoria_retornaLista() {
        Producto p = new Producto();
        when(productoRepository.findByCategoriaId(1L)).thenReturn(List.of(p));
        when(productoMapper.toResponseDTOList(anyList())).thenReturn(List.of(new ProductoResponseDTO()));

        List<ProductoResponseDTO> result = productoService.getProductosByCategoria(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    void getProductosFiltrados_conFiltros() {
        Producto p = new Producto();
        when(productoRepository.filtrarProductos(eq("test"), eq(1L), eq(true), eq(1000), eq(5000), eq(true), eq(false)))
                .thenReturn(List.of(p));
        when(productoMapper.toResponseDTO(p)).thenReturn(new ProductoResponseDTO());

        List<ProductoResponseDTO> result = productoService.getProductosFiltrados("test", 1L, true, 1000, 5000, true, false);

        assertThat(result).hasSize(1);
    }

    @Test
    void getProductosFiltrados_sinFiltros() {
        when(productoRepository.filtrarProductos(null, null, null, null, null, null, null)).thenReturn(List.of());

        List<ProductoResponseDTO> result = productoService.getProductosFiltrados(null, null, null, null, null, null, null);

        assertThat(result).isEmpty();
    }

    @Test
    void getProductosFiltrados_nombreVacio_trataComoNull() {
        when(productoRepository.filtrarProductos(null, null, null, null, null, null, null)).thenReturn(List.of());

        List<ProductoResponseDTO> result = productoService.getProductosFiltrados("", null, null, null, null, null, null);

        assertThat(result).isEmpty();
    }

    @Test
    void createProducto_sinCategoria_setCategoriaNull() {
        ProductoRequestDTO request = new ProductoRequestDTO();
        request.setSku("SKU-NOCAT");
        request.setNombre("Sin Categoria");
        request.setSlug("sin-categoria");

        when(productoRepository.existsBySku("SKU-NOCAT")).thenReturn(false);
        when(productoRepository.existsByNombre("Sin Categoria")).thenReturn(false);
        when(productoRepository.existsBySlug("sin-categoria")).thenReturn(false);

        Producto producto = new Producto();
        producto.setId(5L);
        producto.setCantidad(10);
        when(productoMapper.toEntity(request)).thenReturn(producto);
        when(productoRepository.save(producto)).thenReturn(producto);

        ProductoResponseDTO expectedDto = new ProductoResponseDTO();
        when(productoMapper.toResponseDTO(producto)).thenReturn(expectedDto);

        ProductoResponseDTO result = productoService.createProducto(request);

        assertThat(result).isEqualTo(expectedDto);
        assertThat(producto.getCategoria()).isNull();
    }

    @Test
    void updateProducto_skuDuplicado_lanzaException() {
        ProductoRequestDTO request = new ProductoRequestDTO();
        request.setSku("SKU-002");
        request.setNombre("Producto Original");
        request.setSlug("producto-original");

        Producto productoExistente = new Producto();
        productoExistente.setId(1L);
        productoExistente.setSku("SKU-001");
        productoExistente.setNombre("Producto Original");
        productoExistente.setSlug("producto-original");

        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoExistente));
        when(productoRepository.existsBySku("SKU-002")).thenReturn(true);

        assertThatThrownBy(() -> productoService.updateProducto(1L, request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("SKU");
    }

    @Test
    void updateProducto_nombreDuplicado_lanzaException() {
        ProductoRequestDTO request = new ProductoRequestDTO();
        request.setSku("SKU-001");
        request.setNombre("Nombre Duplicado");
        request.setSlug("producto-original");

        Producto productoExistente = new Producto();
        productoExistente.setId(1L);
        productoExistente.setSku("SKU-001");
        productoExistente.setNombre("Producto Original");
        productoExistente.setSlug("producto-original");

        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoExistente));
        when(productoRepository.existsByNombre("Nombre Duplicado")).thenReturn(true);

        assertThatThrownBy(() -> productoService.updateProducto(1L, request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("nombre");
    }

    @Test
    void updateProducto_slugDuplicado_lanzaException() {
        ProductoRequestDTO request = new ProductoRequestDTO();
        request.setSku("SKU-001");
        request.setNombre("Producto Original");
        request.setSlug("slug-duplicado");

        Producto productoExistente = new Producto();
        productoExistente.setId(1L);
        productoExistente.setSku("SKU-001");
        productoExistente.setNombre("Producto Original");
        productoExistente.setSlug("producto-original");

        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoExistente));
        when(productoRepository.existsBySlug("slug-duplicado")).thenReturn(true);

        assertThatThrownBy(() -> productoService.updateProducto(1L, request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("slug");
    }

    @Test
    void updateProducto_categoriaNoExiste_lanzaResourceNotFoundException() {
        ProductoRequestDTO request = new ProductoRequestDTO();
        request.setSku("SKU-001");
        request.setNombre("Producto Original");
        request.setSlug("producto-original");
        request.setCategoriaId(999L);

        Producto productoExistente = new Producto();
        productoExistente.setId(1L);
        productoExistente.setSku("SKU-001");
        productoExistente.setNombre("Producto Original");
        productoExistente.setSlug("producto-original");

        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoExistente));
        when(categoriaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productoService.updateProducto(1L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Categoría");
    }

    @Test
    void getProductoBySlug_ok() {
        Producto producto = new Producto();
        producto.setId(1L);
        when(productoRepository.findBySlug("producto-test")).thenReturn(Optional.of(producto));
        ProductoResponseDTO expectedDto = new ProductoResponseDTO();
        when(productoMapper.toResponseDTO(producto)).thenReturn(expectedDto);

        ProductoResponseDTO result = productoService.getProductoBySlug("producto-test");

        assertThat(result).isEqualTo(expectedDto);
    }

    @Test
    void getProductoBySku_ok() {
        Producto producto = new Producto();
        producto.setId(1L);
        when(productoRepository.findBySku("SKU-001")).thenReturn(Optional.of(producto));
        ProductoResponseDTO expectedDto = new ProductoResponseDTO();
        when(productoMapper.toResponseDTO(producto)).thenReturn(expectedDto);

        ProductoResponseDTO result = productoService.getProductoBySku("SKU-001");

        assertThat(result).isEqualTo(expectedDto);
    }
}
