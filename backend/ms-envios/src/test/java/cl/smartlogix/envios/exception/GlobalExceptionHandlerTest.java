package cl.smartlogix.envios.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private HttpServletRequest request;

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleNotFound_returns404() {
        when(request.getRequestURI()).thenReturn("/api/envios/999");
        ResourceNotFoundException ex = new ResourceNotFoundException("Envío no encontrado");

        ProblemDetail pd = handler.handleNotFound(ex, request);

        assertEquals(HttpStatus.NOT_FOUND.value(), pd.getStatus());
        assertEquals("Recurso no encontrado", pd.getTitle());
        assertEquals("Envío no encontrado", pd.getDetail());
        assertEquals("/api/envios/999", pd.getInstance().toString());
    }

    @Test
    void handleDomain_returns422() {
        when(request.getRequestURI()).thenReturn("/api/envios");
        DomainException ex = new DomainException("Estado inválido");

        ProblemDetail pd = handler.handleDomain(ex, request);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), pd.getStatus());
        assertEquals("Regla de negocio violada", pd.getTitle());
        assertEquals("Estado inválido", pd.getDetail());
    }

    @Test
    void handleIllegalArgument_returns400() {
        when(request.getRequestURI()).thenReturn("/api/envios/1/estado");
        IllegalArgumentException ex = new IllegalArgumentException("Transición de estado inválida: ENTREGADO -> EN_PROCESO");

        ProblemDetail pd = handler.handleValidationExceptions(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST.value(), pd.getStatus());
        assertEquals("Transición de Estado Inválida", pd.getTitle());
        assertEquals("Transición de estado inválida: ENTREGADO -> EN_PROCESO", pd.getDetail());
    }

    @Test
    void handleGeneric_returns500() {
        when(request.getRequestURI()).thenReturn("/api/envios");
        Exception ex = new RuntimeException("Error inesperado");

        ProblemDetail pd = handler.handleGeneric(ex, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), pd.getStatus());
        assertEquals("Error interno", pd.getTitle());
    }

    @Test
    void handleValidation_returns400() {
        when(request.getRequestURI()).thenReturn("/api/envios");
        var target = new cl.smartlogix.envios.dto.response.EnvioResponseDTO();
        var bindingResult = new org.springframework.validation.BeanPropertyBindingResult(target, "envio");
        bindingResult.rejectValue("pedidoId", "required", "El ID del pedido es obligatorio");
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        ProblemDetail pd = handler.handleValidation(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST.value(), pd.getStatus());
        assertEquals("Error de validación", pd.getTitle());
        assertNotNull(pd.getProperties().get("errors"));
        assertInstanceOf(Map.class, pd.getProperties().get("errors"));
    }
}
