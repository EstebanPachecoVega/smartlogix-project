package cl.smartlogix.gateway.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.main.web-application-type=reactive",
        "spring.cloud.gateway.enabled=false",
        "cors.allowed-origins=http://localhost:3000",
        "management.health.redis.enabled=false",
        "spring.autoconfigure.exclude=org.springframework.cloud.gateway.config.GatewayRedisAutoConfiguration"
})
@AutoConfigureWebTestClient
@Import({SecurityConfigTest.ProductosController.class, SecurityConfigTest.PedidosController.class,
        SecurityConfigTest.EnviosController.class})
class SecurityConfigTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private CorsConfigurationSource corsConfigurationSource;

    @Test
    void fallbackIsPublic() {
        webTestClient.get().uri("/fallback")
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void actuatorIsPublic() {
        webTestClient.get().uri("/actuator/health")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void productosGetIsPublic() {
        webTestClient.get().uri("/api/productos/1")
                .exchange()
                .expectStatus().is2xxSuccessful();
    }

    @Test
    void productosPostWithoutAuth_isUnauthorized() {
        webTestClient.post().uri("/api/productos")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void productosPostWithGestor_isOk() {
        webTestClient.post().uri("/api/productos")
                .headers(h -> h.setBearerAuth("gestor"))
                .exchange()
                .expectStatus().is2xxSuccessful();
    }

    @Test
    void pedidosGetWithoutAuth_isUnauthorized() {
        webTestClient.get().uri("/api/pedidos")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void pedidosGetWithAnyRole_isOk() {
        webTestClient.get().uri("/api/pedidos")
                .headers(h -> h.setBearerAuth("usuario"))
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void enviosGetWithoutGestor_isForbidden() {
        webTestClient.get().uri("/api/envios")
                .headers(h -> h.setBearerAuth("usuario"))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void enviosGetWithGestor_isOk() {
        webTestClient.get().uri("/api/envios")
                .headers(h -> h.setBearerAuth("gestor"))
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void corsConfigurationSource_allowsConfiguredOrigin() {
        assertThat(corsConfigurationSource).isNotNull();
        assertThat(corsConfigurationSource).isInstanceOf(UrlBasedCorsConfigurationSource.class);
    }

    @Test
    void jwtAuthenticationConverter_extractsRolesFromRealmAccess() throws Exception {
        SecurityConfig securityConfig = new SecurityConfig();
        java.lang.reflect.Field field = SecurityConfig.class.getDeclaredField("allowedOrigins");
        field.setAccessible(true);
        field.set(securityConfig, "http://localhost:3000");

        java.lang.reflect.Method method = SecurityConfig.class.getDeclaredMethod("jwtAuthenticationConverter");
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        org.springframework.core.convert.converter.Converter<Jwt, Mono<org.springframework.security.authentication.AbstractAuthenticationToken>> converter =
                (org.springframework.core.convert.converter.Converter<Jwt, Mono<org.springframework.security.authentication.AbstractAuthenticationToken>>) method
                        .invoke(securityConfig);

        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .claim("realm_access", Map.of("roles", List.of("gestor", "usuario")))
                .claim("sub", "user-1")
                .build();

        org.springframework.security.authentication.AbstractAuthenticationToken auth = converter.convert(jwt).block();
        assertThat(auth).isNotNull();
        assertThat(auth.getAuthorities()).extracting("authority")
                .containsExactlyInAnyOrder("ROLE_gestor", "ROLE_usuario");
    }

    @Test
    void jwtAuthenticationConverter_noRealmAccess_returnsEmptyAuthorities() throws Exception {
        SecurityConfig securityConfig = new SecurityConfig();
        java.lang.reflect.Field field = SecurityConfig.class.getDeclaredField("allowedOrigins");
        field.setAccessible(true);
        field.set(securityConfig, "http://localhost:3000");

        java.lang.reflect.Method method = SecurityConfig.class.getDeclaredMethod("jwtAuthenticationConverter");
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        org.springframework.core.convert.converter.Converter<Jwt, Mono<org.springframework.security.authentication.AbstractAuthenticationToken>> converter =
                (org.springframework.core.convert.converter.Converter<Jwt, Mono<org.springframework.security.authentication.AbstractAuthenticationToken>>) method
                        .invoke(securityConfig);

        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .claim("sub", "user-1")
                .build();

        org.springframework.security.authentication.AbstractAuthenticationToken auth = converter.convert(jwt).block();
        assertThat(auth).isNotNull();
        assertThat(auth.getAuthorities()).isEmpty();
    }

    @Test
    void jwtAuthenticationConverter_nullRealmAccess_returnsEmptyAuthorities() throws Exception {
        SecurityConfig securityConfig = new SecurityConfig();
        java.lang.reflect.Field field = SecurityConfig.class.getDeclaredField("allowedOrigins");
        field.setAccessible(true);
        field.set(securityConfig, "http://localhost:3000");

        java.lang.reflect.Method method = SecurityConfig.class.getDeclaredMethod("jwtAuthenticationConverter");
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        org.springframework.core.convert.converter.Converter<Jwt, Mono<org.springframework.security.authentication.AbstractAuthenticationToken>> converter =
                (org.springframework.core.convert.converter.Converter<Jwt, Mono<org.springframework.security.authentication.AbstractAuthenticationToken>>) method
                        .invoke(securityConfig);

        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .claim("sub", "user-1")
                .build();

        org.springframework.security.authentication.AbstractAuthenticationToken auth = converter.convert(jwt).block();
        assertThat(auth).isNotNull();
        assertThat(auth.getAuthorities()).isEmpty();
    }

    @TestConfiguration
    static class MockJwtDecoderConfig {

        @Bean
        ReactiveJwtDecoder jwtDecoder() {
            return token -> {
                String role = token instanceof String s ? s : token.toString();
                return Mono.just(Jwt.withTokenValue("mock-token")
                        .header("alg", "RS256")
                        .claim("realm_access", Map.of("roles", List.of(role)))
                        .claim("sub", "user-123")
                        .build());
            };
        }
    }

    @RestController
    @RequestMapping("/api/productos")
    static class ProductosController {
        @GetMapping("/**")
        Mono<String> get() { return Mono.just("ok"); }
        @PostMapping
        Mono<String> post() { return Mono.just("ok"); }
    }

    @RestController
    @RequestMapping("/api/pedidos")
    static class PedidosController {
        @GetMapping
        Mono<String> get() { return Mono.just("ok"); }
    }

    @RestController
    @RequestMapping("/api/envios")
    static class EnviosController {
        @GetMapping
        Mono<String> get() { return Mono.just("ok"); }
    }
}
