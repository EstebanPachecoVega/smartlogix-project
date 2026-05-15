package cl.smartlogix.gateway.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public Mono<ProblemDetail> handleNotFound(ResourceNotFoundException ex, ServerWebExchange exchange) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        pd.setTitle("Recurso no encontrado");
        pd.setType(URI.create("https://smartlogix.cl/errors/not-found"));
        pd.setInstance(URI.create(exchange.getRequest().getURI().getPath()));
        return Mono.just(pd);
    }

    @ExceptionHandler(DomainException.class)
    public Mono<ProblemDetail> handleDomain(DomainException ex, ServerWebExchange exchange) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        pd.setTitle("Regla de negocio violada");
        pd.setType(URI.create("https://smartlogix.cl/errors/business-rule"));
        pd.setInstance(URI.create(exchange.getRequest().getURI().getPath()));
        return Mono.just(pd);
    }

    @ExceptionHandler(Exception.class)
    public Mono<ProblemDetail> handleGeneric(Exception ex, ServerWebExchange exchange) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ha ocurrido un error interno en el gateway");
        pd.setTitle("Error interno del gateway");
        pd.setType(URI.create("https://smartlogix.cl/errors/gateway-error"));
        pd.setInstance(URI.create(exchange.getRequest().getURI().getPath()));
        return Mono.just(pd);
    }
}