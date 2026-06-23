package cl.smartlogix.envios.mapper;

import cl.smartlogix.envios.dto.response.EnvioResponseDTO;
import cl.smartlogix.envios.entity.Envio;
import cl.smartlogix.envios.entity.EstadoEnvio;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EnvioMapperImplTest {

    private final EnvioMapperImpl mapper = new EnvioMapperImpl();

    @Test
    void toResponseDTO_mapsAllFields() {
        Envio envio = Envio.builder()
                .id(1L)
                .pedidoId(100L)
                .usuarioId("user-1")
                .destinatario("Juan Perez")
                .calle("Av. Siempre Viva")
                .numero("742")
                .comuna("Santiago")
                .ciudad("Santiago")
                .codigoPostal("8320000")
                .metodoEnvio("ESTANDAR")
                .pesoKg(2.5)
                .dimensiones("30x20x10")
                .estadoEnvio(EstadoEnvio.PENDIENTE)
                .empresaLogistica("LOGIX")
                .numeroTracking("TRK-ABC123")
                .fechaEstimadaEntrega(LocalDate.of(2026, 7, 1))
                .fechaCreacion(LocalDateTime.of(2026, 6, 22, 10, 0))
                .build();

        EnvioResponseDTO dto = mapper.toResponseDTO(envio);

        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals(100L, dto.getPedidoId());
        assertEquals("user-1", dto.getUsuarioId());
        assertEquals("Juan Perez", dto.getDestinatario());
        assertEquals("Av. Siempre Viva", dto.getCalle());
        assertEquals("742", dto.getNumero());
        assertEquals("Santiago", dto.getComuna());
        assertEquals("Santiago", dto.getCiudad());
        assertEquals("8320000", dto.getCodigoPostal());
        assertEquals("ESTANDAR", dto.getMetodoEnvio());
        assertEquals(2.5, dto.getPesoKg());
        assertEquals("30x20x10", dto.getDimensiones());
        assertEquals("PENDIENTE", dto.getEstadoEnvio());
        assertEquals("LOGIX", dto.getEmpresaLogistica());
        assertEquals("TRK-ABC123", dto.getNumeroTracking());
        assertEquals(LocalDate.of(2026, 7, 1), dto.getFechaEstimadaEntrega());
    }

    @Test
    void toResponseDTO_nullEnvio_returnsNull() {
        assertNull(mapper.toResponseDTO(null));
    }
}
