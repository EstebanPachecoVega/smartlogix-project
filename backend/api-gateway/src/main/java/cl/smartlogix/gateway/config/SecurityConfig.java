package cl.smartlogix.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        // Deshabilitar CSRF (stateless API)
        http.csrf(ServerHttpSecurity.CsrfSpec::disable);

        // Permitir todas las peticiones sin autenticación (modo pruebas)
        http.authorizeExchange(exchanges -> exchanges
                .anyExchange().permitAll()
        );

        // descomentar estas líneas para habilitar la seguridad con JWT:
        // http.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {}));
        // http.authorizeExchange(exchanges -> exchanges
        //         .pathMatchers("/actuator/**", "/fallback").permitAll()
        //         .anyExchange().authenticated()
        // );

        return http.build();
    }
}