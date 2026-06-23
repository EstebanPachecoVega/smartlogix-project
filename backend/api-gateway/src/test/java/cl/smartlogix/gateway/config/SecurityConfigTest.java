package cl.smartlogix.gateway.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(properties = {
    "spring.main.web-application-type=reactive",
    "spring.cloud.gateway.enabled=false",
    "cors.allowed-origins=http://localhost:3000",
    "management.health.redis.enabled=false"
})
@AutoConfigureWebTestClient
@EnableAutoConfiguration(excludeName = "org.springframework.cloud.gateway.config.GatewayRedisAutoConfiguration")
class SecurityConfigTest {

    @Autowired
    private WebTestClient webTestClient;

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
}
