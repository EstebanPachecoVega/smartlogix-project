package cl.smartlogix.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.Jwt;
import reactor.core.publisher.Mono;

import java.security.Principal;

@Configuration
public class RateLimiterConfig {

    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> exchange.getPrincipal()
                .map(Principal::getName)
                .defaultIfEmpty("anonymous")
                .onErrorResume(e -> {
                    String ip = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
                    return Mono.just(ip);
                })
                .switchIfEmpty(Mono.fromCallable(() -> {
                    String ip = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
                    return ip;
                }));
    }
}