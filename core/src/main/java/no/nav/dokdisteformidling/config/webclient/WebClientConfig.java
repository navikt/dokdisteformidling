package no.nav.dokdisteformidling.config.webclient;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class WebClientConfig {

	@Bean
	@Primary
	public WebClient webClient(WebClient.Builder webClientBuilder) {
		return webClientBuilder
				.clone()
				.clientConnector(new ReactorClientHttpConnector(httpClient()))
				.build();
	}

	private HttpClient httpClient() {
		return HttpClient.create().responseTimeout(Duration.ofSeconds(60))
				.proxyWithSystemProperties();
	}
}

