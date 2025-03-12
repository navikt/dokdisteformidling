package no.nav.dokdisteformidling.consumer.rdist001;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdisteformidling.config.props.DokdisteformidlingProperties;
import no.nav.dokdisteformidling.exception.functional.DokdistadminFunctionalException;
import no.nav.dokdisteformidling.exception.technical.AbstractDokdisteformidlingTechnicalException;
import no.nav.dokdisteformidling.exception.technical.DokdistadminTechnicalException;
import no.nav.dokdisteformidling.utils.NavHeadersFilter;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import static no.nav.dokdisteformidling.azure.OAuthEnabledWebClientConfig.CLIENT_REGISTRATION_DOKDISTADMIN;
import static no.nav.dokdisteformidling.constants.DomainConstants.DISTRIBUSJONSKANAL;
import static no.nav.dokdisteformidling.constants.RetryConstants.DELAY_SHORT;
import static no.nav.dokdisteformidling.constants.RetryConstants.MULTIPLIER_SHORT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId;

@Slf4j
@Component
public class AdministrerForsendelseConsumer implements AdministrerForsendelse {

	private final WebClient webClient;

	public AdministrerForsendelseConsumer(WebClient webClient,
										  DokdisteformidlingProperties dokdisteformidlingProperties) {
		this.webClient = webClient.mutate()
				.baseUrl(dokdisteformidlingProperties.getEndpoints().getDokdistadmin().getUrl())
				.defaultHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.filter(new NavHeadersFilter())
				.build();
	}

	@Override
	@Retryable(retryFor = AbstractDokdisteformidlingTechnicalException.class, backoff = @Backoff(delay = DELAY_SHORT, multiplier = MULTIPLIER_SHORT))
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

	@Override
	@Retryable(retryFor = AbstractDokdisteformidlingTechnicalException.class, backoff = @Backoff(delay = DELAY_SHORT, multiplier = MULTIPLIER_SHORT))
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

	@Override
	@Retryable(retryFor = DokdistadminTechnicalException.class, backoff = @Backoff(delay = DELAY_SHORT, multiplier = MULTIPLIER_SHORT))
	public HentEformidlingforsendelserResponse hentEformidlingForsendelser() {
		log.info("hentEformidlingForsendelser henter eformidlingsforsendelser fra rdist001 (dokdistadmin) med distribusjonskanal={}", DISTRIBUSJONSKANAL);

		var response = webClient.get()
				.uri(uriBuilder -> uriBuilder
						.path("/henteformidlingforsendelser")
						.queryParam("distribusjonKanal", DISTRIBUSJONSKANAL)
						.build())
				.attributes(clientRegistrationId(CLIENT_REGISTRATION_DOKDISTADMIN))
				.retrieve()
				.bodyToMono(HentEformidlingforsendelserResponse.class)
				.onErrorMap(this::mapError)
				.block();

		int antallForsendelser = getAntallForsendelser(response);
		log.info("hentEformidlingForsendelser har hentet {} eformidlingsforsendelser fra rdist001 (dokdistadmin) med distribusjonskanal={}",
				antallForsendelser, DISTRIBUSJONSKANAL);

		return response;
	}

	private static int getAntallForsendelser(HentEformidlingforsendelserResponse response) {
		return response != null && response.getForsendelser() != null ? response.getForsendelser().size() : 0;
	}

	private Throwable mapError(Throwable error) {
		if (error instanceof WebClientResponseException response && response.getStatusCode().is4xxClientError()) {
			return new DokdistadminFunctionalException(
					String.format("Kall mot rdist001 feilet funksjonelt med status=%s, feilmelding=%s",
							response.getStatusCode(),
							response.getMessage()),
					error);
		} else {
			return new DokdistadminTechnicalException(
					String.format("Kall mot rdist001 feilet teknisk med feilmelding=%s", error.getMessage()),
					error);
		}
	}

}
