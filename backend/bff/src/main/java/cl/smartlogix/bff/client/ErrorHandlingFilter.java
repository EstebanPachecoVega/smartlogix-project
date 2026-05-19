package cl.smartlogix.bff.client;

import cl.smartlogix.bff.exception.DomainException;
import cl.smartlogix.bff.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import reactor.core.publisher.Mono;

public class ErrorHandlingFilter {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static ExchangeFilterFunction handleErrors() {
        return ExchangeFilterFunction.ofResponseProcessor(response -> {
            HttpStatusCode status = response.statusCode();
            if (status.is4xxClientError() || status.is5xxServerError()) {
                return response.bodyToMono(String.class)
                        .flatMap(body -> {
                            try {
                                ProblemDetail pd = mapper.readValue(body, ProblemDetail.class);
                                String detail = pd.getDetail();
                                if (status == HttpStatus.NOT_FOUND) {
                                    return Mono.error(new ResourceNotFoundException(
                                            detail != null ? detail : "Recurso no encontrado"));
                                } else if (status == HttpStatus.UNPROCESSABLE_ENTITY) {
                                    return Mono.error(new DomainException(
                                            detail != null ? detail : "Regla de negocio violada"));
                                } else {
                                    return Mono.error(new RuntimeException("Error " + status + ": " + detail));
                                }
                            } catch (Exception e) {
                                return Mono.error(new RuntimeException("Error no interpretable: " + body));
                            }
                        });
            }
            return Mono.just(response);
        });
    }
}