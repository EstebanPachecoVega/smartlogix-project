package cl.smartlogix.bff.config;

import io.netty.channel.ChannelOption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;

@Configuration
public class WebClientConfig {

    @Value("${pedidos.url}")
    private String pedidosUrl;

    @Value("${envios.url}")
    private String enviosUrl;

    @Bean
    public WebClient pedidosWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl(pedidosUrl)
                .clientConnector(new ReactorClientHttpConnector(
                        HttpClient.create(ConnectionProvider.create("pedidos", 100))
                                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
                                .responseTimeout(Duration.ofSeconds(3))))
                .build();
    }

    @Bean
    public WebClient enviosWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl(enviosUrl)
                .clientConnector(new ReactorClientHttpConnector(
                        HttpClient.create(ConnectionProvider.create("envios", 100))
                                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
                                .responseTimeout(Duration.ofSeconds(3))))
                .build();
    }
}