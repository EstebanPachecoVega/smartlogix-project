package cl.smartlogix.gateway.config;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.InetSocketAddress;
import java.security.Principal;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimiterConfigTest {

    private final RateLimiterConfig config = new RateLimiterConfig();
    private final KeyResolver resolver = config.userKeyResolver();

    @Test
    void resolve_withPrincipal_returnsPrincipalName() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/test").remoteAddress(new InetSocketAddress("192.168.1.1", 12345)));
        exchange.getPrincipal().subscribe();
        Principal principal = () -> "user-42";

        Mono<String> result = resolver.resolve(exchange.mutate().principal(Mono.just(principal)).build());

        StepVerifier.create(result)
                .expectNext("user-42")
                .verifyComplete();
    }

    @Test
    void resolve_withEmptyPrincipal_returnsAnonymous() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/test").remoteAddress(new InetSocketAddress("192.168.1.2", 12345)));

        Mono<String> result = resolver.resolve(exchange.mutate().principal(Mono.empty()).build());

        StepVerifier.create(result)
                .expectNext("anonymous")
                .verifyComplete();
    }

    @Test
    void resolve_withPrincipalError_fallsBackToIp() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/test").remoteAddress(new InetSocketAddress("10.0.0.99", 12345)));

        Mono<String> result = resolver
                .resolve(exchange.mutate().principal(Mono.error(new RuntimeException("auth error"))).build());

        StepVerifier.create(result)
                .expectNext("10.0.0.99")
                .verifyComplete();
    }
}
