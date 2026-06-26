package cl.smartlogix.envios.service.impl;

import cl.smartlogix.envios.dto.event.EnvioActualizadoEventDTO;
import cl.smartlogix.envios.dto.response.EnvioResponseDTO;
import cl.smartlogix.envios.entity.Envio;
import cl.smartlogix.envios.entity.EstadoEnvio;
import cl.smartlogix.envios.exception.ResourceNotFoundException;
import cl.smartlogix.envios.mapper.EnvioMapper;
import cl.smartlogix.envios.publisher.EnvioEventPublisher;
import cl.smartlogix.envios.repository.EnvioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnvioServiceImplTest {

    @Mock
    private EnvioRepository envioRepository;

    @Mock
    private EnvioMapper envioMapper;

    @Mock
    private EnvioEventPublisher eventPublisher;

    @Captor
    private ArgumentCaptor<EnvioActualizadoEventDTO> eventCaptor;

    private EnvioServiceImpl envioService;

    @BeforeEach
    void setUp() {
        envioService = new EnvioServiceImpl(envioRepository, envioMapper, eventPublisher);
    }

    @Test
    void actualizarEstado_ok_publicaEvento() {
        Envio envio = new Envio();
        envio.setId(1L);
        envio.setPedidoId(10L);
        envio.setEstadoEnvio(EstadoEnvio.PENDIENTE);
        envio.setNumeroTracking("TRK-001");

        when(envioRepository.findById(1L)).thenReturn(Optional.of(envio));

        EnvioResponseDTO expectedDto = new EnvioResponseDTO();
        when(envioMapper.toResponseDTO(envio)).thenReturn(expectedDto);

        EnvioResponseDTO result = envioService.actualizarEstado(1L, EstadoEnvio.ENVIADO);

        assertThat(result).isEqualTo(expectedDto);
        assertThat(envio.getEstadoEnvio()).isEqualTo(EstadoEnvio.ENVIADO);

        verify(envioRepository).save(envio);
        verify(eventPublisher).publicarEnvioActualizado(eventCaptor.capture());
        EnvioActualizadoEventDTO evento = eventCaptor.getValue();
        assertThat(evento.getPedidoId()).isEqualTo(10L);
        assertThat(evento.getEnvioId()).isEqualTo(1L);
        assertThat(evento.getEstadoEnvio()).isEqualTo(EstadoEnvio.ENVIADO);
    }

    @Test
    void actualizarEstado_mismoEstado_omitidoPorIdempotencia() {
        Envio envio = new Envio();
        envio.setId(1L);
        envio.setEstadoEnvio(EstadoEnvio.ENVIADO);

        when(envioRepository.findById(1L)).thenReturn(Optional.of(envio));
        when(envioMapper.toResponseDTO(envio)).thenReturn(new EnvioResponseDTO());

        envioService.actualizarEstado(1L, EstadoEnvio.ENVIADO);

        verify(envioRepository, never()).save(any());
        verify(eventPublisher, never()).publicarEnvioActualizado(any());
    }

    @Test
    void actualizarEstado_retrocesoNoPermitido_lanzaIllegalArgumentException() {
        Envio envio = new Envio();
        envio.setId(1L);
        envio.setEstadoEnvio(EstadoEnvio.ENVIADO);

        when(envioRepository.findById(1L)).thenReturn(Optional.of(envio));

        assertThatThrownBy(() -> envioService.actualizarEstado(1L, EstadoEnvio.PENDIENTE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Retroceso no permitido");
    }

    @Test
    void actualizarEstado_envioNoExiste_lanzaResourceNotFoundException() {
        when(envioRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> envioService.actualizarEstado(999L, EstadoEnvio.ENVIADO))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void listarTodos_retornaLista() {
        Envio e1 = new Envio();
        Envio e2 = new Envio();
        when(envioRepository.findAll()).thenReturn(List.of(e1, e2));
        when(envioMapper.toResponseDTO(e1)).thenReturn(new EnvioResponseDTO());
        when(envioMapper.toResponseDTO(e2)).thenReturn(new EnvioResponseDTO());

        List<EnvioResponseDTO> result = envioService.listarTodos();

        assertThat(result).hasSize(2);
    }

    @Test
    void obtenerPorId_ok() {
        Envio envio = new Envio();
        when(envioRepository.findById(1L)).thenReturn(Optional.of(envio));
        EnvioResponseDTO expectedDto = new EnvioResponseDTO();
        when(envioMapper.toResponseDTO(envio)).thenReturn(expectedDto);

        EnvioResponseDTO result = envioService.obtenerPorId(1L);

        assertThat(result).isEqualTo(expectedDto);
    }

    @Test
    void obtenerPorId_noExiste_lanzaResourceNotFoundException() {
        when(envioRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> envioService.obtenerPorId(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void obtenerPorPedidoId_ok() {
        Envio envio = new Envio();
        when(envioRepository.findByPedidoId(10L)).thenReturn(Optional.of(envio));
        EnvioResponseDTO expectedDto = new EnvioResponseDTO();
        when(envioMapper.toResponseDTO(envio)).thenReturn(expectedDto);

        EnvioResponseDTO result = envioService.obtenerPorPedidoId(10L);

        assertThat(result).isEqualTo(expectedDto);
    }

    @Test
    void obtenerPorPedidoId_noExiste_lanzaResourceNotFoundException() {
        when(envioRepository.findByPedidoId(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> envioService.obtenerPorPedidoId(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void obtenerPorTracking_ok() {
        Envio envio = new Envio();
        when(envioRepository.findByNumeroTracking("TRK-001")).thenReturn(Optional.of(envio));
        EnvioResponseDTO expectedDto = new EnvioResponseDTO();
        when(envioMapper.toResponseDTO(envio)).thenReturn(expectedDto);

        EnvioResponseDTO result = envioService.obtenerPorTracking("TRK-001");

        assertThat(result).isEqualTo(expectedDto);
    }

    @Test
    void obtenerPorTracking_noExiste_lanzaResourceNotFoundException() {
        when(envioRepository.findByNumeroTracking("NO-EXISTE")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> envioService.obtenerPorTracking("NO-EXISTE"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void listarPorEstado_retornaFiltrados() {
        Envio envio = new Envio();
        when(envioRepository.findByEstadoEnvio(EstadoEnvio.EN_TRANSITO)).thenReturn(List.of(envio));
        when(envioMapper.toResponseDTO(envio)).thenReturn(new EnvioResponseDTO());

        List<EnvioResponseDTO> result = envioService.listarPorEstado(EstadoEnvio.EN_TRANSITO);

        assertThat(result).hasSize(1);
    }

    @Test
    void listarEnviosConProblemas_filtraCorrectamente() {
        Envio envioProblema = new Envio();
        envioProblema.setEstadoEnvio(EstadoEnvio.RETRASADO);
        Envio envioDevuelto = new Envio();
        envioDevuelto.setEstadoEnvio(EstadoEnvio.DEVUELTO);

        when(envioRepository.findByEstadoEnvioIn(any())).thenReturn(List.of(envioProblema, envioDevuelto));
        when(envioMapper.toResponseDTO(envioProblema)).thenReturn(new EnvioResponseDTO());
        when(envioMapper.toResponseDTO(envioDevuelto)).thenReturn(new EnvioResponseDTO());

        List<EnvioResponseDTO> result = envioService.listarEnviosConProblemas();

        assertThat(result).hasSize(2);
    }
}
