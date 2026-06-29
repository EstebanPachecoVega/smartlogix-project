package cl.smartlogix.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;

@RestController
public class FallbackController {

    @GetMapping("/fallback")
    public Mono<ProblemDetail> fallback(ServerWebExchange exchange) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.SERVICE_UNAVAILABLE,
                "El servicio no está disponible temporalmente. Intente más tarde.");
        pd.setTitle("Servicio no disponible");
        pd.setType(URI.create("https://smartlogix.cl/errors/service-unavailable"));
        pd.setInstance(URI.create(exchange.getRequest().getURI().getPath()));
        return Mono.just(pd);
    }
}