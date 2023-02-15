package no.nav.dokdisteformidling.azure;

import no.nav.dokdisteformidling.config.props.DokdisteformidlingProperties;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

public class AzureAuthenticationFilter implements ExchangeFilterFunction {

	private final AzureToken azureToken;
	private final DokdisteformidlingProperties.AzureEndpoint endpoint;

	public AzureAuthenticationFilter(AzureToken azureToken, DokdisteformidlingProperties.AzureEndpoint endpoint) {
		this.azureToken = azureToken;
		this.endpoint = endpoint;
	}

	@Override
	public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
		return next.exchange(ClientRequest.from(request)
				.headers(httpHeaders -> {
					httpHeaders.setBearerAuth(azureToken.accessToken(endpoint.getScope()));
				})
				.build());
	}
}
