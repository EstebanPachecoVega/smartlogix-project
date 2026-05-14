package cl.smartlogix.pedidos.client;

import cl.smartlogix.pedidos.exception.DomainException;
import cl.smartlogix.pedidos.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class FeignErrorDecoder implements ErrorDecoder {
    private final ErrorDecoder defaultDecoder = new Default();
    private final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Override
    public Exception decode(String methodKey, Response response) {
        System.out.println("FeignErrorDecoder ejecutándose para: " + methodKey + ", status: " + response.status());
        try {
            String body = new String(response.body().asInputStream().readAllBytes(), StandardCharsets.UTF_8);
            System.out.println("Cuerpo de error: " + body);
            var problemDetail = mapper.readValue(body, org.springframework.http.ProblemDetail.class);
            String detail = problemDetail.getDetail();
            int status = problemDetail.getStatus();

            if (status == HttpStatus.NOT_FOUND.value()) {
                return new ResourceNotFoundException(detail != null ? detail : "Recurso no encontrado");
            } else if (status == HttpStatus.UNPROCESSABLE_ENTITY.value()) {
                return new DomainException(detail != null ? detail : "Regla de negocio violada");
            } else {
                return new ResponseStatusException(HttpStatus.valueOf(status), detail);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return defaultDecoder.decode(methodKey, response);
        }
    }
}