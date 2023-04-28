package no.nav.dokdisteformidling.consumer.rdist001;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdisteformidling.azure.AzureAuthenticationFilter;
import no.nav.dokdisteformidling.azure.AzureToken;
import no.nav.dokdisteformidling.config.props.DokdisteformidlingProperties;
import no.nav.dokdisteformidling.exception.functional.DokdistadminFunctionalException;
import no.nav.dokdisteformidling.exception.technical.AbstractDokdisteformidlingTechnicalException;
import no.nav.dokdisteformidling.exception.technical.DokdistadminTechnicalException;
import no.nav.dokdisteformidling.metrics.Monitor;
import no.nav.dokdisteformidling.utils.NavHeadersFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import static no.nav.dokdisteformidling.constants.DomainConstants.DISTRIBUSJONSKANAL;
import static no.nav.dokdisteformidling.constants.RetryConstants.DELAY_SHORT;
import static no.nav.dokdisteformidling.constants.RetryConstants.MULTIPLIER_SHORT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@Component
public class AdministrerForsendelseConsumer implements AdministrerForsendelse {

	private final WebClient webClient;

	@Autowired
	public AdministrerForsendelseConsumer(WebClient webClient,
										  DokdisteformidlingProperties dokdisteformidlingProperties,
										  AzureToken azureToken) {
		this.webClient = webClient.mutate()
				.baseUrl(dokdisteformidlingProperties.getEndpoints().getDokdistadmin().getUrl())
				.defaultHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.filter(new AzureAuthenticationFilter(azureToken, dokdisteformidlingProperties.getEndpoints().getDokdistadmin()))
				.filter(new NavHeadersFilter())
				.build();
	}

	@Override
	@Retryable(include = AbstractDokdisteformidlingTechnicalException.class, backoff = @Backoff(delay = DELAY_SHORT, multiplier = MULTIPLIER_SHORT))
	@Monitor(value = "dok_consumer", extraTags = {"process", "hentForsendelse"}, histogram = true)
	public HentForsendelseResponse hentForsendelse(final Long forsendelseId) {
		log.info("hentForsendelse henter forsendelse med forsendelseId={}", forsendelseId);

		var response = webClient.get()
				.uri(uriBuilder -> uriBuilder
						.path("/{forsendelseId}")
						.build(forsendelseId))
				.retrieve()
				.bodyToMono(HentForsendelseResponse.class)
				.doOnError(this::handleError)
				.block();

		log.info("hentForsendelse har hentet forsendelse med forsendelseId={}", forsendelseId);

		return response;
	}

	@Override
	@Retryable(include = AbstractDokdisteformidlingTechnicalException.class, backoff = @Backoff(delay = DELAY_SHORT, multiplier = MULTIPLIER_SHORT))
	@Monitor(value = "dok_consumer", extraTags = {"process", "oppdaterForsendelseStatus"}, histogram = true)
	public void oppdaterForsendelseStatusOgKonversasjonsId(OppdaterForsendelseRequest oppdaterForsendelseRequest) {
		log.info("oppdaterForsendelseStatusOgKonversasjonsId mottatt kall til å oppdatere forsendelse med forsendelseId={}", oppdaterForsendelseRequest.getForsendelseId());

		webClient.put()
				.uri("/oppdaterforsendelse")
				.bodyValue(oppdaterForsendelseRequest)
				.retrieve()
				.toBodilessEntity()
				.doOnError(this::handleError)
				.block();
	}

	@Override
	@Retryable(include = DokdistadminTechnicalException.class, backoff = @Backoff(delay = DELAY_SHORT, multiplier = MULTIPLIER_SHORT))
	@Monitor(value = "dok_consumer", extraTags = {"process", "hentEformidlingForsendelser"}, histogram = true)
	public HentEformidlingforsendelserResponseTo hentEformidlingForsendelser() {
		log.info("hentEformidlingForsendelser henter eformidlingsforsendelser fra rdist001 (dokdistadmin) med distribusjonskanal={}", DISTRIBUSJONSKANAL);

		var response = webClient.get()
				.uri(uriBuilder -> uriBuilder
						.path("/henteformidlingforsendelser")
						.queryParam("distribusjonKanal", DISTRIBUSJONSKANAL)
						.build())
				.retrieve()
				.bodyToMono(HentEformidlingforsendelserResponseTo.class)
				.doOnError(this::handleError)
				.block();

		int antallForsendelser = getAntallForsendelser(response);
		log.info("hentEformidlingForsendelser har hentet {} eformidlingsforsendelser fra rdist001 (dokdistadmin) med distribusjonskanal={}",
				antallForsendelser, DISTRIBUSJONSKANAL);

		return response;
	}

	private static int getAntallForsendelser(HentEformidlingforsendelserResponseTo response) {
		return response != null && response.getForsendelser() != null ? response.getForsendelser().size() : 0;
	}

	private void handleError(Throwable error) {
		if (error instanceof WebClientResponseException response && ((WebClientResponseException) error).getStatusCode().is4xxClientError()) {
			throw new DokdistadminFunctionalException(
					String.format("Kall mot rdist001 feilet funksjonelt med status=%s, feilmelding=%s",
							response.getRawStatusCode(),
							response.getMessage()),
					error);
		} else {
			throw new DokdistadminTechnicalException(
					String.format("Kall mot rdist001 feilet teknisk med feilmelding=%s", error.getMessage()),
					error);
		}
	}

}
