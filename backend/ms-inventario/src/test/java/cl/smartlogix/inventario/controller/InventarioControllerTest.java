package cl.smartlogix.inventario.controller;

import cl.smartlogix.inventario.dto.request.CancelarReservaRequestDTO;
import cl.smartlogix.inventario.dto.request.ConfirmarReservaRequestDTO;
import cl.smartlogix.inventario.dto.request.PedidoStockRequestDTO;
import cl.smartlogix.inventario.dto.request.ReservarStockRequestDTO;
import cl.smartlogix.inventario.dto.response.StockResponseDTO;
import cl.smartlogix.inventario.entity.Producto;
import cl.smartlogix.inventario.exception.DomainException;
import cl.smartlogix.inventario.exception.ResourceNotFoundException;
import cl.smartlogix.inventario.mapper.ProductoMapper;
import cl.smartlogix.inventario.repository.ProductoRepository;
import cl.smartlogix.inventario.service.InventarioService;
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
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InventarioController.class)
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser(roles = "gestor")
class InventarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private InventarioService inventarioService;

    @MockitoBean
    private ProductoRepository productoRepository;

    @MockitoBean
    private ProductoMapper productoMapper;

    @Test
    void reservarStock_200() throws Exception {
        PedidoStockRequestDTO request = new PedidoStockRequestDTO(
                List.of(new ReservarStockRequestDTO(1L, 2)), "res-1");

        when(inventarioService.reservarStockLote(anyList(), anyString())).thenReturn("res-1");

        mockMvc.perform(post("/api/inventario/reservar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservaId").value("res-1"));
    }

    @Test
    void reservarStock_sinItems_400() throws Exception {
        mockMvc.perform(post("/api/inventario/reservar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void reservarStock_stockInsuficiente_422() throws Exception {
        PedidoStockRequestDTO request = new PedidoStockRequestDTO(
                List.of(new ReservarStockRequestDTO(99L, 999)), "res-fail");

        when(inventarioService.reservarStockLote(anyList(), anyString()))
                .thenThrow(new DomainException("Stock insuficiente"));

        mockMvc.perform(post("/api/inventario/reservar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void confirmarReserva_200() throws Exception {
        ConfirmarReservaRequestDTO request = new ConfirmarReservaRequestDTO(
                "res-1",
                List.of(new ConfirmarReservaRequestDTO.ItemConfirmacionDTO(1L, 2)));

        mockMvc.perform(post("/api/inventario/confirmar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void cancelarReserva_200() throws Exception {
        CancelarReservaRequestDTO request = new CancelarReservaRequestDTO(
                "res-1",
                List.of(new CancelarReservaRequestDTO.ItemCancelacionDTO(1L, 2)));

        mockMvc.perform(post("/api/inventario/cancelar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void obtenerStockDisponible_200() throws Exception {
        Producto producto = new Producto();
        producto.setId(1L);
        producto.setCantidad(50);

        StockResponseDTO stockDto = new StockResponseDTO(1L, 50);

        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(productoMapper.toStockResponseDTO(producto)).thenReturn(stockDto);

        mockMvc.perform(get("/api/inventario/stock/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productoId").value(1))
                .andExpect(jsonPath("$.stockDisponible").value(50));
    }

    @Test
    void obtenerStockDisponible_noExiste_404() throws Exception {
        when(productoRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/inventario/stock/999"))
                .andExpect(status().isNotFound());
    }
}
