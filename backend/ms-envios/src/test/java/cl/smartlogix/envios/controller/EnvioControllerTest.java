package cl.smartlogix.envios.controller;

import cl.smartlogix.envios.dto.response.EnvioResponseDTO;
import cl.smartlogix.envios.entity.EstadoEnvio;
import cl.smartlogix.envios.service.EnvioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EnvioController.class)
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser(roles = "gestor")
class EnvioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EnvioService envioService;

    @Test
    void listarEnvios_200() throws Exception {
        EnvioResponseDTO e1 = new EnvioResponseDTO();
        e1.setId(1L);
        e1.setPedidoId(10L);
        EnvioResponseDTO e2 = new EnvioResponseDTO();
        e2.setId(2L);
        e2.setPedidoId(20L);

        when(envioService.listarTodos(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(e1, e2)));

        mockMvc.perform(get("/api/envios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(1));
    }

    @Test
    void obtenerEnvio_200() throws Exception {
        EnvioResponseDTO envio = new EnvioResponseDTO();
        envio.setId(1L);
        envio.setPedidoId(10L);
        envio.setNumeroTracking("TRACK-001");

        when(envioService.obtenerPorId(1L)).thenReturn(envio);

        mockMvc.perform(get("/api/envios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.numeroTracking").value("TRACK-001"));
    }

    @Test
    void obtenerEnvioPorPedidoId_200() throws Exception {
        EnvioResponseDTO envio = new EnvioResponseDTO();
        envio.setId(1L);
        envio.setPedidoId(10L);

        when(envioService.obtenerPorPedidoId(10L)).thenReturn(envio);

        mockMvc.perform(get("/api/envios/pedido/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pedidoId").value(10));
    }

    @Test
    void obtenerEnvioPorTracking_200() throws Exception {
        EnvioResponseDTO envio = new EnvioResponseDTO();
        envio.setId(1L);
        envio.setNumeroTracking("TRACK-001");

        when(envioService.obtenerPorTracking("TRACK-001")).thenReturn(envio);

        mockMvc.perform(get("/api/envios/tracking/TRACK-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numeroTracking").value("TRACK-001"));
    }

    @Test
    void listarEnviosConProblemas_200() throws Exception {
        EnvioResponseDTO envio = new EnvioResponseDTO();
        envio.setId(1L);
        envio.setEstadoEnvio("EN_PROBLEMAS");

        when(envioService.listarEnviosConProblemas()).thenReturn(List.of(envio));

        mockMvc.perform(get("/api/envios/problemas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].estadoEnvio").value("EN_PROBLEMAS"));
    }

    @Test
    void actualizarEstadoEnvio_200() throws Exception {
        EnvioResponseDTO envio = new EnvioResponseDTO();
        envio.setId(1L);
        envio.setEstadoEnvio("EN_TRANSITO");

        when(envioService.actualizarEstado(eq(1L), eq(EstadoEnvio.EN_TRANSITO))).thenReturn(envio);

        mockMvc.perform(patch("/api/envios/1/estado?nuevoEstado=EN_TRANSITO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estadoEnvio").value("EN_TRANSITO"));
    }

    @Test
    void eliminarEnvio_204() throws Exception {
        mockMvc.perform(delete("/api/envios/1"))
                .andExpect(status().isNoContent());
    }
}
