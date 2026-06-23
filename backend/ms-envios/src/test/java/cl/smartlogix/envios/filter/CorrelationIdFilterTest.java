package cl.smartlogix.envios.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
    @Mock
    private FilterChain chain;

    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @Test
    void doFilter_usesExistingCorrelationId() throws Exception {
        when(request.getHeader("X-Correlation-Id")).thenReturn("existing-id");

        doAnswer(invocation -> {
            assertEquals("existing-id", MDC.get("correlationId"));
            return null;
        }).when(chain).doFilter(request, response);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    void doFilter_generatesNewCorrelationIdWhenMissing() throws Exception {
        when(request.getHeader("X-Correlation-Id")).thenReturn(null);

        doAnswer(invocation -> {
            assertNotNull(MDC.get("correlationId"));
            return null;
        }).when(chain).doFilter(request, response);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    void doFilter_cleansMdcAfterRequest() throws Exception {
        when(request.getHeader("X-Correlation-Id")).thenReturn("test-id");

        filter.doFilter(request, response, chain);

        assertNull(MDC.get("correlationId"));
    }
}
