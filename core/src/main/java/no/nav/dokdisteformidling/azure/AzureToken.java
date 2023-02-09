package no.nav.dokdisteformidling.azure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokdisteformidling.exception.functional.AzureTokenFunctionalException;
import no.nav.dokdisteformidling.exception.technical.AzureTokenTechnicalException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import static no.nav.dokdisteformidling.config.cache.LokalCacheConfig.AZURE_TOKEN_CACHE;
import static no.nav.dokdisteformidling.constants.RetryConstants.DELAY_SHORT;
import static no.nav.dokdisteformidling.constants.RetryConstants.MULTIPLIER_SHORT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;

@Slf4j
@Component
public class AzureToken {

	private final WebClient webClient;
	private final ObjectMapper objectMapper;
	private final AzureConfig azureConfig;

	public AzureToken(WebClient webClient,
					  ObjectMapper objectMapper,
					  AzureConfig azureConfig) {
		this.webClient = webClient.mutate()
				.baseUrl(azureConfig.getOpenidConfigTokenEndpoint())
				.defaultHeader(CONTENT_TYPE, APPLICATION_FORM_URLENCODED_VALUE)
				.build();
		this.objectMapper = objectMapper;
		this.azureConfig = azureConfig;
	}

	@Retryable(include = AzureTokenTechnicalException.class, backoff = @Backoff(delay = DELAY_SHORT, multiplier = MULTIPLIER_SHORT))
	@Cacheable(AZURE_TOKEN_CACHE)
	public String accessToken(String scope) {
		return fetchAccessToken(scope);
	}

	private String fetchAccessToken(String scope) {

		MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
		formData.add("client_id", azureConfig.getAppClientId());
		formData.add("client_secret", azureConfig.getAppClientSecret());
		formData.add("grant_type", "client_credentials");
		formData.add("scope", scope);

		String responseJson = webClient.post()
				.body(BodyInserters.fromFormData(formData))
				.retrieve()
				.bodyToMono(String.class)
				.doOnError(this::handleError)
				.block();

		try {
			return objectMapper.readValue(responseJson, TokenResponse.class).accessToken();
		} catch (JsonProcessingException e) {
			throw new AzureTokenFunctionalException(String.format("Klarte ikke parse token fra Azure. Feilmelding=%s", e.getMessage()), e);
		}
	}

	private void handleError(Throwable error) {
		if (error instanceof WebClientResponseException response && ((WebClientResponseException) error).getStatusCode().is4xxClientError()) {
			throw new AzureTokenFunctionalException(
					String.format("Klarte ikke hente token fra Azure. Feilet med statuskode=%s Feilmelding=%s",
							response.getRawStatusCode(),
							response.getMessage()),
					error);
		} else {
			throw new AzureTokenTechnicalException(
					String.format("Kall mot Azure feilet med feilmelding=%s", error.getMessage()),
					error);
		}
	}
}
