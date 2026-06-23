package cl.smartlogix.pedidos.client;

import feign.RequestTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FeignAuthInterceptorTest {

    private final FeignAuthInterceptor interceptor = new FeignAuthInterceptor();

    @Test
    void apply_withJwtAuth_addsBearerHeader() {
        Jwt jwt = Jwt.withTokenValue("my-jwt-token")
                .header("alg", "RS256")
                .claim("sub", "user")
                .build();
        JwtAuthenticationToken auth = new JwtAuthenticationToken(jwt);
        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(context);

        RequestTemplate template = new RequestTemplate();
        interceptor.apply(template);

        assertTrue(template.headers().containsKey("Authorization"));
        assertEquals("Bearer my-jwt-token", template.headers().get("Authorization").iterator().next());
        SecurityContextHolder.clearContext();
    }

    @Test
    void apply_withoutAuth_doesNothing() {
        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(context);

        RequestTemplate template = new RequestTemplate();
        interceptor.apply(template);

        assertFalse(template.headers().containsKey("Authorization"));
        SecurityContextHolder.clearContext();
    }
}
