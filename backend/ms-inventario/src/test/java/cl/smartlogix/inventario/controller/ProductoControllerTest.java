package cl.smartlogix.inventario.controller;

import cl.smartlogix.inventario.dto.request.ProductoRequestDTO;
import cl.smartlogix.inventario.dto.response.MapaCategoriaResponseDTO;
import cl.smartlogix.inventario.dto.response.ProductoResponseDTO;
import cl.smartlogix.inventario.exception.DuplicateResourceException;
import cl.smartlogix.inventario.exception.ResourceNotFoundException;
import cl.smartlogix.inventario.repository.ProductoRepository;
import cl.smartlogix.inventario.service.ProductoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductoController.class)
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser(roles = "gestor")
class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductoService productoService;

    @MockitoBean
    private ProductoRepository productoRepository;

    @Test
    void createProducto_201() throws Exception {
        ProductoRequestDTO request = new ProductoRequestDTO();
        request.setSku("SKU-001");
        request.setNombre("Producto Test");
        request.setSlug("producto-test");
        request.setPrecio(5000);
        request.setCantidad(10);

        ProductoResponseDTO response = new ProductoResponseDTO();
        response.setId(1L);
        response.setSku("SKU-001");

        when(productoService.createProducto(any())).thenReturn(response);

        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.sku").value("SKU-001"));
    }

    @Test
    void createProducto_skuDuplicado_409() throws Exception {
        ProductoRequestDTO request = new ProductoRequestDTO();
        request.setSku("SKU-001");
        request.setNombre("Producto Test");
        request.setSlug("producto-test");
        request.setPrecio(5000);
        request.setCantidad(10);

        when(productoService.createProducto(any()))
                .thenThrow(new DuplicateResourceException("El SKU SKU-001 ya existe"));

        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void createProducto_categoriaNoExiste_422() throws Exception {
        ProductoRequestDTO request = new ProductoRequestDTO();
        request.setSku("SKU-001");
        request.setNombre("Producto Test");
        request.setSlug("producto-test");
        request.setPrecio(5000);
        request.setCantidad(10);
        request.setCategoriaId(999L);

        when(productoService.createProducto(any()))
                .thenThrow(new ResourceNotFoundException("Categoría no encontrada"));

        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createProducto_sinSku_400() throws Exception {
        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Test\",\"slug\":\"test\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateProducto_200() throws Exception {
        ProductoRequestDTO request = new ProductoRequestDTO();
        request.setSku("SKU-002");
        request.setNombre("Actualizado");
        request.setSlug("actualizado");
        request.setPrecio(6000);
        request.setCantidad(20);

        ProductoResponseDTO response = new ProductoResponseDTO();
        response.setId(1L);
        response.setNombre("Actualizado");

        when(productoService.updateProducto(eq(1L), any())).thenReturn(response);

        mockMvc.perform(put("/api/productos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Actualizado"));
    }

    @Test
    void updateProducto_noExiste_404() throws Exception {
        ProductoRequestDTO request = new ProductoRequestDTO();
        request.setSku("SKU-002");
        request.setNombre("Actualizado");
        request.setSlug("actualizado");
        request.setPrecio(6000);
        request.setCantidad(20);

        when(productoService.updateProducto(eq(999L), any()))
                .thenThrow(new ResourceNotFoundException("Producto no encontrado"));

        mockMvc.perform(put("/api/productos/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteProducto_204() throws Exception {
        mockMvc.perform(delete("/api/productos/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteProducto_noExiste_404() throws Exception {
        doThrow(new ResourceNotFoundException("Producto no encontrado"))
                .when(productoService).deleteProducto(999L);

        mockMvc.perform(delete("/api/productos/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getProductoById_200() throws Exception {
        ProductoResponseDTO response = new ProductoResponseDTO();
        response.setId(1L);
        response.setNombre("Producto Test");
        response.setSku("SKU-001");

        when(productoService.getProductoById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/productos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Producto Test"));
    }

    @Test
    void getProductoById_noExiste_404() throws Exception {
        when(productoService.getProductoById(999L))
                .thenThrow(new ResourceNotFoundException("Producto no encontrado"));

        mockMvc.perform(get("/api/productos/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllProductos_200() throws Exception {
        ProductoResponseDTO p1 = new ProductoResponseDTO();
        p1.setId(1L);
        p1.setNombre("Producto 1");
        ProductoResponseDTO p2 = new ProductoResponseDTO();
        p2.setId(2L);
        p2.setNombre("Producto 2");

        when(productoService.getAllProductos()).thenReturn(List.of(p1, p2));

        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getProductoBySlug_200() throws Exception {
        ProductoResponseDTO response = new ProductoResponseDTO();
        response.setId(1L);
        response.setSlug("producto-test");

        when(productoService.getProductoBySlug("producto-test")).thenReturn(response);

        mockMvc.perform(get("/api/productos/slug/producto-test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("producto-test"));
    }

    @Test
    void getProductoBySku_200() throws Exception {
        ProductoResponseDTO response = new ProductoResponseDTO();
        response.setId(1L);
        response.setSku("SKU-001");

        when(productoService.getProductoBySku("SKU-001")).thenReturn(response);

        mockMvc.perform(get("/api/productos/sku/SKU-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value("SKU-001"));
    }

    @Test
    void getProductosByCategoria_200() throws Exception {
        ProductoResponseDTO p = new ProductoResponseDTO();
        p.setId(1L);

        when(productoService.getProductosByCategoria(1L)).thenReturn(List.of(p));

        mockMvc.perform(get("/api/productos/categoria/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getProductosFiltrados_conParametros() throws Exception {
        ProductoResponseDTO p = new ProductoResponseDTO();
        p.setId(1L);

        when(productoService.getProductosFiltrados(eq("test"), eq(1L), eq(true), eq(1000), eq(5000), eq(true), eq(false)))
                .thenReturn(List.of(p));

        mockMvc.perform(get("/api/productos")
                        .param("nombre", "test")
                        .param("categoriaId", "1")
                        .param("conStock", "true")
                        .param("precioMin", "1000")
                        .param("precioMax", "5000")
                        .param("destacado", "true")
                        .param("novedad", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getMapaCategorias_200() throws Exception {
        MapaCategoriaResponseDTO m = new MapaCategoriaResponseDTO(1L, "Electrónica");
        when(productoRepository.findMapaCategorias()).thenReturn(List.of(m));

        mockMvc.perform(get("/api/productos/mapa-categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].productoId").value(1));
    }
}
