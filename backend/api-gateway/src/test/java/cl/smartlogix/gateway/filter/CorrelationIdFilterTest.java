package cl.smartlogix.gateway.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CorrelationIdFilterTest {

    @Mock
    private GatewayFilterChain chain;

    @Captor
    private ArgumentCaptor<ServerWebExchange> exchangeCaptor;

    private CorrelationIdFilter filter;

    @BeforeEach
    void setUp() {
        filter = new CorrelationIdFilter();
        when(chain.filter(exchangeCaptor.capture())).thenReturn(Mono.empty());
    }

    @Test
    void filter_usesExistingCorrelationId() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/test")
                .header("X-Correlation-Id", "my-corr-id")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        ServerWebExchange mutated = exchangeCaptor.getValue();
        assertEquals("my-corr-id", mutated.getRequest().getHeaders().getFirst("X-Correlation-Id"));
        assertEquals("my-corr-id", exchange.getResponse().getHeaders().getFirst("X-Correlation-Id"));
    }

    @Test
    void filter_generatesCorrelationIdWhenMissing() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        ServerWebExchange mutated = exchangeCaptor.getValue();
        assertNotNull(mutated.getRequest().getHeaders().getFirst("X-Correlation-Id"));
        assertNotNull(exchange.getResponse().getHeaders().getFirst("X-Correlation-Id"));
    }
}
