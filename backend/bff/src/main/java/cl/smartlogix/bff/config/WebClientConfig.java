package cl.smartlogix.bff.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${gateway.url}")
    private String gatewayUrl;

    @Bean
    public WebClient gatewayWebClient(WebClient.Builder builder) {
        return builder.baseUrl(gatewayUrl).build();
    }
}