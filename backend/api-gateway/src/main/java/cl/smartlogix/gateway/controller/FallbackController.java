package cl.smartlogix.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

import java.net.URI;

@RestController
public class FallbackController {

    @GetMapping("/fallback")
    public Mono<ProblemDetail> fallback() {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.SERVICE_UNAVAILABLE,
                "El servicio no está disponible en este momento. Intente más tarde.");
        pd.setTitle("Servicio no disponible");
        pd.setType(URI.create("https://smartlogix.cl/errors/service-unavailable"));
        return Mono.just(pd);
    }
}