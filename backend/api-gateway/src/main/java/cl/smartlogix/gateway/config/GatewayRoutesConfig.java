package cl.smartlogix.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class GatewayRoutesConfig {

    @Bean
    public RedisRateLimiter redisRateLimiter() {
        return new RedisRateLimiter(
                10,
                20,
                1
        );
    }

    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {

            var remoteAddress = exchange.getRequest().getRemoteAddress();

            if (remoteAddress == null) {
                return Mono.just("anonymous");
            }

            return Mono.just(
                    remoteAddress
                            .getAddress()
                            .getHostAddress()
            );
        };
    }

    @Bean
    public RouteLocator customRouteLocator(
            RouteLocatorBuilder builder,
            RedisRateLimiter redisRateLimiter,
            KeyResolver userKeyResolver
    ) {

        return builder.routes()

                .route("bff_route", r -> r
                        .path("/bff/**")

                        .filters(f -> f

                                .circuitBreaker(config -> config
                                        .setName("bffCircuitBreaker")
                                        .setFallbackUri("forward:/fallback")
                                )

                                .requestRateLimiter(config -> config
                                        .setRateLimiter(redisRateLimiter)
                                        .setKeyResolver(userKeyResolver)
                                )
                        )

                        .uri("http://localhost:8084")
                )

                .build();
    }
}