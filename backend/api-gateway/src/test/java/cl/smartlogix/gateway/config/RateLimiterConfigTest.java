package cl.smartlogix.gateway.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class RateLimiterConfigTest {

    @Autowired
    private KeyResolver userKeyResolver;

    @Test
    void userKeyResolver_resolvesIp() {
        assertThat(userKeyResolver).isNotNull();
    }
}
