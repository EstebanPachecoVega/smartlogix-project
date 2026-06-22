package cl.smartlogix.inventario.controller;

import cl.smartlogix.inventario.dto.request.CategoriaRequestDTO;
import cl.smartlogix.inventario.dto.request.ReordenarCategoriaDTO;
import cl.smartlogix.inventario.dto.response.CategoriaResponseDTO;
import cl.smartlogix.inventario.exception.DuplicateResourceException;
import cl.smartlogix.inventario.exception.ResourceNotFoundException;
import cl.smartlogix.inventario.service.CategoriaService;
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

@WebMvcTest(CategoriaController.class)
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser(roles = "gestor")
class CategoriaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CategoriaService categoriaService;

    @Test
    void createCategoria_201() throws Exception {
        CategoriaRequestDTO request = new CategoriaRequestDTO();
        request.setNombre("Electrónica");
        request.setSlug("electronica");

        CategoriaResponseDTO response = new CategoriaResponseDTO();
        response.setId(1L);
        response.setNombre("Electrónica");

        when(categoriaService.createCategoria(any())).thenReturn(response);

        mockMvc.perform(post("/api/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Electrónica"));
    }

    @Test
    void createCategoria_nombreDuplicado_409() throws Exception {
        CategoriaRequestDTO request = new CategoriaRequestDTO();
        request.setNombre("Electrónica");
        request.setSlug("electronica");

        when(categoriaService.createCategoria(any()))
                .thenThrow(new DuplicateResourceException("La categoría ya existe"));

        mockMvc.perform(post("/api/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void createCategoria_invalido_400() throws Exception {
        mockMvc.perform(post("/api/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateCategoria_200() throws Exception {
        CategoriaRequestDTO request = new CategoriaRequestDTO();
        request.setNombre("Actualizada");
        request.setSlug("actualizada");

        CategoriaResponseDTO response = new CategoriaResponseDTO();
        response.setId(1L);
        response.setNombre("Actualizada");

        when(categoriaService.updateCategoria(eq(1L), any())).thenReturn(response);

        mockMvc.perform(put("/api/categorias/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Actualizada"));
    }

    @Test
    void updateCategoria_noExiste_404() throws Exception {
        when(categoriaService.updateCategoria(eq(999L), any()))
                .thenThrow(new ResourceNotFoundException("Categoría no encontrada"));

        CategoriaRequestDTO request = new CategoriaRequestDTO();
        request.setNombre("Test");
        request.setSlug("test");

        mockMvc.perform(put("/api/categorias/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteCategoria_204() throws Exception {
        mockMvc.perform(delete("/api/categorias/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteCategoria_conProductos_422() throws Exception {
        doThrow(new cl.smartlogix.inventario.exception.DomainException(
                "No se puede eliminar la categoría porque tiene productos asociados"))
                .when(categoriaService).deleteCategoria(1L);

        mockMvc.perform(delete("/api/categorias/1"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void getCategoriaById_200() throws Exception {
        CategoriaResponseDTO response = new CategoriaResponseDTO();
        response.setId(1L);
        response.setNombre("Electrónica");

        when(categoriaService.getCategoriaById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/categorias/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Electrónica"));
    }

    @Test
    void getCategoriaById_noExiste_404() throws Exception {
        when(categoriaService.getCategoriaById(999L))
                .thenThrow(new ResourceNotFoundException("Categoría no encontrada"));

        mockMvc.perform(get("/api/categorias/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCategoriaBySlug_200() throws Exception {
        CategoriaResponseDTO response = new CategoriaResponseDTO();
        response.setId(1L);
        response.setSlug("electronica");

        when(categoriaService.getCategoriaBySlug("electronica")).thenReturn(response);

        mockMvc.perform(get("/api/categorias/slug/electronica"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("electronica"));
    }

    @Test
    void getAllCategorias_200() throws Exception {
        CategoriaResponseDTO c1 = new CategoriaResponseDTO();
        c1.setId(1L);
        CategoriaResponseDTO c2 = new CategoriaResponseDTO();
        c2.setId(2L);

        when(categoriaService.getAllCategorias()).thenReturn(List.of(c1, c2));

        mockMvc.perform(get("/api/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getCategoriasPrincipales_200() throws Exception {
        when(categoriaService.getCategoriasPrincipales())
                .thenReturn(List.of(new CategoriaResponseDTO()));

        mockMvc.perform(get("/api/categorias/principales"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void reordenar_204() throws Exception {
        List<ReordenarCategoriaDTO> ordenes = List.of(
                new ReordenarCategoriaDTO(1L, 1),
                new ReordenarCategoriaDTO(2L, 2)
        );

        mockMvc.perform(patch("/api/categorias/reordenar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ordenes)))
                .andExpect(status().isNoContent());
    }
}
