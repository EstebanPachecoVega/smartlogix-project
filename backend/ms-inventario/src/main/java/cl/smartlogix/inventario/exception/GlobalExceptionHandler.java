package cl.smartlogix.inventario.exception;

import jakarta.validation.ConstraintViolationException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(ResourceNotFoundException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        pd.setTitle("Recurso no encontrado");
        pd.setType(URI.create("https://smartlogix.cl/errors/not-found"));
        pd.setProperty("timestamp", System.currentTimeMillis());
        return pd;
    }

    @ExceptionHandler(DomainException.class)
    public ProblemDetail handleDomain(DomainException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        pd.setTitle("Regla de negocio violada");
        pd.setType(URI.create("https://smartlogix.cl/errors/business-rule"));
        pd.setProperty("timestamp", System.currentTimeMillis());
        return pd;
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ProblemDetail handleDuplicate(DuplicateResourceException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        pd.setTitle("Recurso ya existe");
        pd.setType(URI.create("https://smartlogix.cl/errors/duplicate-resource"));
        pd.setProperty("timestamp", System.currentTimeMillis());
        return pd;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("Error de validación");
        pd.setDetail("Los campos enviados no son válidos");
        pd.setType(URI.create("https://smartlogix.cl/errors/validation"));
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        pd.setProperty("errors", errors);
        pd.setProperty("timestamp", System.currentTimeMillis());
        return pd;
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ProblemDetail handleHandlerMethodValidation(HandlerMethodValidationException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("Error de validación en parámetros");
        pd.setDetail("Uno o más parámetros no son válidos");
        pd.setType(URI.create("https://smartlogix.cl/errors/validation"));
        return pd;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        pd.setTitle("Error interno del servidor");
        pd.setDetail("Ha ocurrido un error inesperado. Por favor, intente más tarde.");
        pd.setType(URI.create("https://smartlogix.cl/errors/internal-server-error"));
        pd.setProperty("timestamp", System.currentTimeMillis());
        return pd;
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        pd.setTitle("Conflicto de datos en la Base de Datos");
        pd.setDetail(
                "No se pudo completar la operación. Es posible que esté intentando registrar un valor que ya existe (ej: nombre o slug duplicado) o violando una restricción de la base de datos.");
        pd.setType(URI.create("https://smartlogix.cl/errors/data-conflict"));
        pd.setProperty("timestamp", System.currentTimeMillis());
        return pd;
    }
}