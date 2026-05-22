package no.nav.dokdisteformidling.consumer.rdist001;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdisteformidling.config.props.DokdisteformidlingProperties;
import no.nav.dokdisteformidling.exception.functional.DokdistadminFunctionalException;
import no.nav.dokdisteformidling.exception.technical.AbstractDokdisteformidlingTechnicalException;
import no.nav.dokdisteformidling.exception.technical.DokdistadminTechnicalException;
import no.nav.dokdisteformidling.utils.NavHeadersFilter;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import static no.nav.dokdisteformidling.azure.OAuthEnabledWebClientConfig.CLIENT_REGISTRATION_DOKDISTADMIN;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId;

@Slf4j
@Component
public class AdministrerForsendelseConsumer {

	private final WebClient webClient;

	public AdministrerForsendelseConsumer(WebClient webClient,
										  DokdisteformidlingProperties dokdisteformidlingProperties) {
		this.webClient = webClient.mutate()
				.baseUrl(dokdisteformidlingProperties.getEndpoints().getDokdistadmin().getUrl())
				.defaultHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.codecs(codecs -> codecs.defaultCodecs()
						.maxInMemorySize((int) DataSize.ofMegabytes(1).toBytes()))
				.filter(new NavHeadersFilter())
				.build();
	}

	@Retryable(value = AbstractDokdisteformidlingTechnicalException.class)
	public HentForsendelseResponse hentForsendelse(final Long forsendelseId) {
		return webClient.get()
				.uri(uriBuilder -> uriBuilder
						.path("/{forsendelseId}")
						.build(forsendelseId))
				.attributes(clientRegistrationId(CLIENT_REGISTRATION_DOKDISTADMIN))
				.retrieve()
				.bodyToMono(HentForsendelseResponse.class)
				.onErrorMap(this::mapError)
				.block();
	}

	@Retryable(value = AbstractDokdisteformidlingTechnicalException.class)
	public void oppdaterForsendelse(OppdaterForsendelseRequest oppdaterForsendelse) {
		webClient.put()
				.uri("/oppdaterforsendelse")
				.attributes(clientRegistrationId(CLIENT_REGISTRATION_DOKDISTADMIN))
				.bodyValue(oppdaterForsendelse)
				.retrieve()
				.toBodilessEntity()
				.onErrorMap(this::mapError)
				.block();
	}

	private Throwable mapError(Throwable error) {
		if (error instanceof WebClientResponseException response && response.getStatusCode().is4xxClientError()) {
			return new DokdistadminFunctionalException(
					"Kall mot rdist001 feilet funksjonelt med status=%s, feilmelding=%s".formatted(
							response.getStatusCode(),
							response.getMessage()),
					error);
		} else {
			return new DokdistadminTechnicalException(
					"Kall mot rdist001 feilet teknisk med feilmelding=%s".formatted(error.getMessage()),
					error);
		}
	}

}
