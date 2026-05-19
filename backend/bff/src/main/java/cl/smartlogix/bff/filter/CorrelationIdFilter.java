package cl.smartlogix.bff.filter;

import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@Order(1)
public class CorrelationIdFilter implements WebFilter {
    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String correlationId = exchange.getRequest().getHeaders().getFirst(CORRELATION_ID_HEADER);
        if (correlationId == null) correlationId = UUID.randomUUID().toString();
        final String finalCorrelationId = correlationId;
        MDC.put("correlationId", finalCorrelationId);
        ServerWebExchange mutated = exchange.mutate()
                .request(r -> r.header(CORRELATION_ID_HEADER, finalCorrelationId))
                .build();
        return chain.filter(mutated)
                .doFinally(s -> MDC.remove("correlationId"));
    }
}