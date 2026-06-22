package cl.smartlogix.pedidos.exception;

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
        when(request.getRequestURI()).thenReturn("/api/pedidos/999");
        ResourceNotFoundException ex = new ResourceNotFoundException("Pedido no encontrado");

        ProblemDetail pd = handler.handleNotFound(ex, request);

        assertEquals(HttpStatus.NOT_FOUND.value(), pd.getStatus());
        assertEquals("Recurso no encontrado", pd.getTitle());
        assertEquals("Pedido no encontrado", pd.getDetail());
        assertEquals("/api/pedidos/999", pd.getInstance().toString());
    }

    @Test
    void handleDomain_returns422() {
        when(request.getRequestURI()).thenReturn("/api/pedidos");
        DomainException ex = new DomainException("Stock insuficiente");

        ProblemDetail pd = handler.handleDomain(ex, request);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), pd.getStatus());
        assertEquals("Regla de negocio violada", pd.getTitle());
        assertEquals("Stock insuficiente", pd.getDetail());
    }

    @Test
    void handleGeneric_returns500() {
        when(request.getRequestURI()).thenReturn("/api/pedidos");
        Exception ex = new RuntimeException("Error inesperado");

        ProblemDetail pd = handler.handleGeneric(ex, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), pd.getStatus());
        assertEquals("Error interno", pd.getTitle());
    }

    @Test
    void handleValidation_returns400() {
        when(request.getRequestURI()).thenReturn("/api/pedidos");
        var target = new cl.smartlogix.pedidos.dto.request.CrearPedidoRequestDTO();
        var bindingResult = new org.springframework.validation.BeanPropertyBindingResult(target, "pedido");
        bindingResult.rejectValue("destinatario", "required", "El destinatario es obligatorio");
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        ProblemDetail pd = handler.handleValidation(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST.value(), pd.getStatus());
        assertEquals("Error de validación", pd.getTitle());
        assertNotNull(pd.getProperties().get("errors"));
        assertInstanceOf(Map.class, pd.getProperties().get("errors"));
    }
}
