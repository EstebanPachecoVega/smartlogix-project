package cl.smartlogix.pedidos.client;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component
public class FeignCorrelationIdInterceptor implements RequestInterceptor {
    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    @Override
    public void apply(RequestTemplate template) {
        String correlationId = MDC.get("correlationId");
        if (correlationId != null) {
            template.header(CORRELATION_ID_HEADER, correlationId);
        }
    }
}