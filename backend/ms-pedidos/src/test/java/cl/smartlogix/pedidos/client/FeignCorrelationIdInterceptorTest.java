package cl.smartlogix.pedidos.client;

import feign.RequestTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.junit.jupiter.api.Assertions.*;

class FeignCorrelationIdInterceptorTest {

    private final FeignCorrelationIdInterceptor interceptor = new FeignCorrelationIdInterceptor();

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void apply_withCorrelationId_addsHeader() {
        MDC.put("correlationId", "corr-123");

        RequestTemplate template = new RequestTemplate();
        interceptor.apply(template);

        assertTrue(template.headers().containsKey("X-Correlation-Id"));
        assertEquals("corr-123", template.headers().get("X-Correlation-Id").iterator().next());
    }

    @Test
    void apply_withoutCorrelationId_doesNothing() {
        RequestTemplate template = new RequestTemplate();
        interceptor.apply(template);

        assertFalse(template.headers().containsKey("X-Correlation-Id"));
    }
}
