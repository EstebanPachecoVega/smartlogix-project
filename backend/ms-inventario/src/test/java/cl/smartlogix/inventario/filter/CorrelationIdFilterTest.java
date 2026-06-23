package cl.smartlogix.inventario.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CorrelationIdFilterTest {

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;

    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @BeforeEach
    void setUp() {
        MDC.clear();
    }

    @Test
    void doFilter_usesExistingCorrelationId() throws Exception {
        when(request.getHeader("X-Correlation-Id")).thenReturn("existing-id");

        FilterChain capturingChain = (req, res) ->
            assertEquals("existing-id", MDC.get("correlationId"));

        filter.doFilter(request, response, capturingChain);

        assertNull(MDC.get("correlationId"));
    }

    @Test
    void doFilter_generatesNewCorrelationIdWhenMissing() throws Exception {
        when(request.getHeader("X-Correlation-Id")).thenReturn(null);

        FilterChain capturingChain = (req, res) ->
            assertNotNull(MDC.get("correlationId"));

        filter.doFilter(request, response, capturingChain);

        assertNull(MDC.get("correlationId"));
    }

    @Test
    void doFilter_generatesNewCorrelationIdWhenBlank() throws Exception {
        when(request.getHeader("X-Correlation-Id")).thenReturn("  ");

        FilterChain capturingChain = (req, res) ->
            assertNotNull(MDC.get("correlationId"));

        filter.doFilter(request, response, capturingChain);

        assertNull(MDC.get("correlationId"));
    }

    @Test
    void doFilter_cleansMdcAfterRequest() throws Exception {
        when(request.getHeader("X-Correlation-Id")).thenReturn("test-id");

        filter.doFilter(request, response, (req, res) -> {});

        assertNull(MDC.get("correlationId"));
    }
}
