package no.nav.dokdisteformidling.consumer.rdist001;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdisteformidling.azure.AzureAuthenticationFilter;
import no.nav.dokdisteformidling.azure.AzureToken;
import no.nav.dokdisteformidling.config.alias.ServiceuserAlias;
import no.nav.dokdisteformidling.config.props.DokdisteformidlingProperties;
import no.nav.dokdisteformidling.exception.functional.DokdistadminFunctionalException;
import no.nav.dokdisteformidling.exception.functional.Rdist001HentForsendelseFunctionalException;
import no.nav.dokdisteformidling.exception.functional.Rdist001OppdaterForsendelseFunctionalException;
import no.nav.dokdisteformidling.exception.technical.AbstractDokdisteformidlingTechnicalException;
import no.nav.dokdisteformidling.exception.technical.DokdistadminTechnicalException;
import no.nav.dokdisteformidling.exception.technical.Rdist001HentForsendelseTechnicalException;
import no.nav.dokdisteformidling.exception.technical.Rdist001OppdaterForsendelseTechnicalException;
import no.nav.dokdisteformidling.metrics.Monitor;
import no.nav.dokdisteformidling.utils.NavHeadersFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;

import static no.nav.dokdisteformidling.constants.DomainConstants.DISTRIBUSJONSKANAL;
import static no.nav.dokdisteformidling.constants.NavHeaders.NAV_CALLID;
import static no.nav.dokdisteformidling.constants.RetryConstants.DELAY_SHORT;
import static no.nav.dokdisteformidling.constants.RetryConstants.MULTIPLIER_SHORT;
import static no.nav.dokdisteformidling.utils.MDCUtils.getCallId;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@Component
public class AdministrerForsendelseConsumer implements AdministrerForsendelse {

	private final String administrerforsendelseV1Url;

	private final RestTemplate restTemplate;
	private final WebClient webClient;

	@Autowired
	public AdministrerForsendelseConsumer(@Value("${administrerforsendelse.v1.url}") String administrerforsendelseV1Url,
										  RestTemplateBuilder restTemplateBuilder,
										  final ServiceuserAlias serviceuserAlias,
										  WebClient webClient,
										  DokdisteformidlingProperties dokdisteformidlingProperties,
										  AzureToken azureToken) {
		this.administrerforsendelseV1Url = administrerforsendelseV1Url;
		this.restTemplate = restTemplateBuilder
				.setReadTimeout(Duration.ofSeconds(20))
				.setConnectTimeout(Duration.ofSeconds(5))
				.basicAuthentication(serviceuserAlias.getUsername(), serviceuserAlias.getPassword())
				.build();
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
	public HentForsendelseResponseTo hentForsendelse(final String forsendelseId) {
		try {
			HttpEntity entity = new HttpEntity<>(createHeaders());
			return restTemplate.exchange(this.administrerforsendelseV1Url + "/" + forsendelseId, HttpMethod.GET, entity, HentForsendelseResponseTo.class)
					.getBody();
		} catch (HttpClientErrorException e) {
			throw new Rdist001HentForsendelseFunctionalException(String.format("Kall mot rdist001 - hentForsendelse feilet funksjonelt med statusKode=%s, feilmelding=%s", e
					.getStatusCode(), e.getMessage()), e);
		} catch (HttpServerErrorException e) {
			throw new Rdist001HentForsendelseTechnicalException(String.format("Kall mot rdist001 - hentForsendelse feilet teknisk med statusKode=%s, feilmelding=%s", e
					.getStatusCode(), e.getMessage()), e);
		}
	}

	@Retryable(include = AbstractDokdisteformidlingTechnicalException.class, backoff = @Backoff(delay = DELAY_SHORT, multiplier = MULTIPLIER_SHORT))
	@Monitor(value = "dok_consumer", extraTags = {"process", "oppdaterForsendelseStatus"}, histogram = true)
	public void oppdaterForsendelseStatus(String forsendelseId, String forsendelseStatus) {
		String uri = UriComponentsBuilder.fromHttpUrl(administrerforsendelseV1Url)
				.queryParam("forsendelseId", forsendelseId)
				.queryParam("forsendelseStatus", forsendelseStatus)
				.toUriString();
		oppdaterForsendelse(uri);
	}

	@Retryable(include = AbstractDokdisteformidlingTechnicalException.class, backoff = @Backoff(delay = DELAY_SHORT, multiplier = MULTIPLIER_SHORT))
	@Monitor(value = "dok_consumer", extraTags = {"process", "oppdaterForsendelseStatusOgKonversasjonsId"}, histogram = true)
	public void oppdaterForsendelseStatusOgKonversasjonsId(String forsendelseId, String forsendelseStatus, String konversasjonsId) {
		String uri = UriComponentsBuilder.fromHttpUrl(administrerforsendelseV1Url)
				.queryParam("forsendelseId", forsendelseId)
				.queryParam("forsendelseStatus", forsendelseStatus)
				.queryParam("konversasjonsId", konversasjonsId)
				.toUriString();
		log.info("Kaller rdist001 med forsendelseId={}, konversasjonsId={} til forsendelseStatus={}", forsendelseId, konversasjonsId, forsendelseStatus);
		oppdaterForsendelse(uri);
	}

	private void oppdaterForsendelse(String uri) {
		try {
			HttpEntity entity = new HttpEntity<>(createHeaders());
			restTemplate.exchange(uri, HttpMethod.PUT, entity, Object.class);
		} catch (HttpClientErrorException e) {
			throw new Rdist001OppdaterForsendelseFunctionalException(String.format("Kall mot rdist001 - oppdaterForsendelse feilet funksjonelt med statusKode=%s, feilmelding=%s", e
					.getStatusCode(), e.getMessage()), e);
		} catch (HttpServerErrorException e) {
			throw new Rdist001OppdaterForsendelseTechnicalException(String.format("Kall mot rdist001 - oppdaterForsendelse feilet teknisk med statusKode=%s, feilmelding=%s", e
					.getStatusCode(), e.getMessage()), e);
		}
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

	private HttpHeaders createHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set(NAV_CALLID, getCallId());
		return headers;
	}

}
