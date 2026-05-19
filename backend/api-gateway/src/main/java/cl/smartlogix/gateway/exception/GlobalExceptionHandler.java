package cl.smartlogix.gateway.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;

@Component
@Order(-2)
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ProblemDetail pd;
        HttpStatus status;

        if (ex instanceof ResourceNotFoundException) {
            status = HttpStatus.NOT_FOUND;
            pd = ProblemDetail.forStatusAndDetail(status, ex.getMessage());
            pd.setTitle("Recurso no encontrado");
            pd.setType(URI.create("https://smartlogix.cl/errors/not-found"));
        } else if (ex instanceof DomainException) {
            status = HttpStatus.UNPROCESSABLE_ENTITY;
            pd = ProblemDetail.forStatusAndDetail(status, ex.getMessage());
            pd.setTitle("Regla de negocio violada");
            pd.setType(URI.create("https://smartlogix.cl/errors/business-rule"));
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            pd = ProblemDetail.forStatusAndDetail(status, "Error interno del gateway");
            pd.setTitle("Error interno");
            pd.setType(URI.create("https://smartlogix.cl/errors/gateway-error"));
        }

        pd.setInstance(URI.create(exchange.getRequest().getURI().getPath()));
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_PROBLEM_JSON);

        try {
            byte[] bytes = mapper.writeValueAsBytes(pd);
            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
            return exchange.getResponse().writeWith(Mono.just(buffer));
        } catch (Exception e) {
            return exchange.getResponse().setComplete();
        }
    }
}