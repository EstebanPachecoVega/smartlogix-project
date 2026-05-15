package cl.smartlogix.gateway.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutesConfig {

    @Autowired
    private RedisRateLimiter redisRateLimiter;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("bff_route", r -> r
                        .path("/bff/**")
                        .filters(f -> f
                                .circuitBreaker(config -> config
                                        .setName("bffCircuitBreaker")
                                        .setFallbackUri("forward:/fallback"))
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(redisRateLimiter)
                                        .setKeyResolver(new RateLimiterConfig().userKeyResolver()))
                        )
                        .uri("lb://bff"))
                .build();
    }
}