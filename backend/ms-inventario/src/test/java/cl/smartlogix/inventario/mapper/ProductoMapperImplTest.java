package cl.smartlogix.inventario.mapper;

import cl.smartlogix.inventario.dto.request.ProductoRequestDTO;
import cl.smartlogix.inventario.dto.response.ProductoResponseDTO;
import cl.smartlogix.inventario.dto.response.StockResponseDTO;
import cl.smartlogix.inventario.entity.Categoria;
import cl.smartlogix.inventario.entity.Producto;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProductoMapperImplTest {

    private final ProductoMapperImpl mapper = new ProductoMapperImpl();

    @Test
    void toEntity_mapsAllFields() {
        ProductoRequestDTO dto = new ProductoRequestDTO();
        dto.setSku("SKU-001");
        dto.setNombre("Producto Test");
        dto.setSlug("producto-test");
        dto.setDescripcion("Descripción");
        dto.setCategoriaId(5L);
        dto.setPrecio(10000);
        dto.setCantidad(20);

        Producto entity = mapper.toEntity(dto);

        assertEquals("SKU-001", entity.getSku());
        assertEquals("Producto Test", entity.getNombre());
        assertEquals("producto-test", entity.getSlug());
        assertEquals("Descripción", entity.getDescripcion());
        assertNotNull(entity.getCategoria());
        assertEquals(5L, entity.getCategoria().getId());
        assertEquals(10000, entity.getPrecio());
        assertEquals(20, entity.getCantidad());
    }

    @Test
    void toEntity_nullCategoriaId_setsNullCategoria() {
        ProductoRequestDTO dto = new ProductoRequestDTO();
        dto.setSku("SKU-002");
        dto.setNombre("Producto Sin Categoría");
        dto.setSlug("producto-sin-categoria");
        dto.setPrecio(5000);
        dto.setCantidad(10);

        Producto entity = mapper.toEntity(dto);

        assertNull(entity.getCategoria());
    }

    @Test
    void toResponseDTO_mapsAllFields() {
        Categoria categoria = new Categoria();
        categoria.setId(5L);
        categoria.setNombre("Electrónica");

        Producto entity = Producto.builder()
                .id(1L)
                .sku("SKU-001")
                .nombre("Producto Test")
                .slug("producto-test")
                .descripcion("Descripción")
                .categoria(categoria)
                .precio(10000)
                .cantidad(20)
                .destacado(true)
                .novedad(false)
                .build();

        ProductoResponseDTO dto = mapper.toResponseDTO(entity);

        assertEquals(1L, dto.getId());
        assertEquals("SKU-001", dto.getSku());
        assertEquals("Producto Test", dto.getNombre());
        assertEquals("producto-test", dto.getSlug());
        assertEquals(5L, dto.getCategoriaId());
        assertEquals("Electrónica", dto.getCategoriaNombre());
        assertEquals(10000, dto.getPrecio());
        assertEquals(20, dto.getCantidad());
        assertTrue(dto.getDestacado());
        assertFalse(dto.getNovedad());
    }

    @Test
    void toResponseDTO_nullCategoria_returnsNullFields() {
        Producto entity = Producto.builder()
                .id(1L)
                .sku("SKU-001")
                .nombre("Test")
                .slug("test")
                .precio(5000)
                .cantidad(10)
                .build();

        ProductoResponseDTO dto = mapper.toResponseDTO(entity);

        assertNull(dto.getCategoriaId());
        assertNull(dto.getCategoriaNombre());
    }

    @Test
    void toResponseDTOList_mapsAll() {
        Producto p1 = Producto.builder().id(1L).sku("SKU-001").nombre("P1").slug("p1").precio(100).cantidad(1).build();
        Producto p2 = Producto.builder().id(2L).sku("SKU-002").nombre("P2").slug("p2").precio(200).cantidad(2).build();

        List<ProductoResponseDTO> dtos = mapper.toResponseDTOList(List.of(p1, p2));

        assertEquals(2, dtos.size());
        assertEquals(1L, dtos.get(0).getId());
        assertEquals(2L, dtos.get(1).getId());
    }

    @Test
    void toStockResponseDTO_mapsFields() {
        Producto entity = Producto.builder()
                .id(1L)
                .sku("SKU-001")
                .nombre("Test")
                .slug("test")
                .precio(5000)
                .cantidad(50)
                .build();

        StockResponseDTO dto = mapper.toStockResponseDTO(entity);

        assertEquals(1L, dto.getProductoId());
        assertEquals(50, dto.getStockDisponible());
    }

    @Test
    void updateEntity_updatesNonNullFields() {
        Categoria categoria = new Categoria();
        categoria.setId(1L);

        Producto entity = Producto.builder()
                .id(1L)
                .sku("SKU-001")
                .nombre("Original")
                .slug("original")
                .precio(5000)
                .cantidad(10)
                .build();

        ProductoRequestDTO dto = new ProductoRequestDTO();
        dto.setNombre("Actualizado");
        dto.setPrecio(6000);
        dto.setCategoriaId(2L);

        mapper.updateEntity(entity, dto);

        assertEquals("Actualizado", entity.getNombre());
        assertEquals(6000, entity.getPrecio());
        assertNotNull(entity.getCategoria());
        assertEquals(2L, entity.getCategoria().getId());

        assertEquals("SKU-001", entity.getSku());
        assertEquals("original", entity.getSlug());
        assertEquals(10, entity.getCantidad());
    }
}
