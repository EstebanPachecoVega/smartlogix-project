package cl.smartlogix.inventario.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

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
        when(request.getRequestURI()).thenReturn("/api/productos/999");
        ResourceNotFoundException ex = new ResourceNotFoundException("Producto no encontrado");

        ProblemDetail pd = handler.handleNotFound(ex, request);

        assertEquals(HttpStatus.NOT_FOUND.value(), pd.getStatus());
        assertEquals("Recurso no encontrado", pd.getTitle());
        assertEquals("Producto no encontrado", pd.getDetail());
        assertEquals("/api/productos/999", pd.getInstance().toString());
    }

    @Test
    void handleDomain_returns422() {
        when(request.getRequestURI()).thenReturn("/api/inventario/reservar");
        DomainException ex = new DomainException("Stock insuficiente");

        ProblemDetail pd = handler.handleDomain(ex, request);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), pd.getStatus());
        assertEquals("Regla de negocio violada", pd.getTitle());
        assertEquals("Stock insuficiente", pd.getDetail());
    }

    @Test
    void handleDuplicate_returns409() {
        when(request.getRequestURI()).thenReturn("/api/productos");
        DuplicateResourceException ex = new DuplicateResourceException("El SKU ya existe");

        ProblemDetail pd = handler.handleDuplicate(ex, request);

        assertEquals(HttpStatus.CONFLICT.value(), pd.getStatus());
        assertEquals("Recurso ya existe", pd.getTitle());
        assertEquals("El SKU ya existe", pd.getDetail());
    }

    @Test
    void handleGeneric_returns500() {
        when(request.getRequestURI()).thenReturn("/api/productos");
        Exception ex = new RuntimeException("Error inesperado");

        ProblemDetail pd = handler.handleGeneric(ex, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), pd.getStatus());
        assertEquals("Error interno del servidor", pd.getTitle());
    }

    @Test
    void handleDataIntegrityViolation_returns409() {
        when(request.getRequestURI()).thenReturn("/api/productos");
        var ex = new org.springframework.dao.DataIntegrityViolationException("Duplicate entry");

        ProblemDetail pd = handler.handleDataIntegrityViolation(ex, request);

        assertEquals(HttpStatus.CONFLICT.value(), pd.getStatus());
        assertEquals("Conflicto de datos en la Base de Datos", pd.getTitle());
    }

    @Test
    void handleValidation_returns400() {
        when(request.getRequestURI()).thenReturn("/api/productos");
        var target = new cl.smartlogix.inventario.dto.request.ProductoRequestDTO();
        var bindingResult = new org.springframework.validation.BeanPropertyBindingResult(target, "producto");
        bindingResult.rejectValue("nombre", "required", "El nombre es obligatorio");
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        ProblemDetail pd = handler.handleValidation(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST.value(), pd.getStatus());
        assertEquals("Error de validación", pd.getTitle());
        assertNotNull(pd.getProperties().get("errors"));
        assertInstanceOf(Map.class, pd.getProperties().get("errors"));
    }

    @Test
    void handleHandlerMethodValidation_returns400() {
        HandlerMethodValidationException ex = org.mockito.Mockito.mock(HandlerMethodValidationException.class);
        when(request.getRequestURI()).thenReturn("/api/parametros");

        ProblemDetail pd = handler.handleHandlerMethodValidation(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST.value(), pd.getStatus());
        assertEquals("Error de validación en parámetros", pd.getTitle());
    }
}
