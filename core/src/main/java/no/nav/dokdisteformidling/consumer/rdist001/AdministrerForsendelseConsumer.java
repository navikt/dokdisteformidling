package no.nav.dokdisteformidling.consumer.rdist001;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdisteformidling.config.alias.ServiceuserAlias;
import no.nav.dokdisteformidling.constants.MdcConstants;
import no.nav.dokdisteformidling.constants.RetryConstants;
import no.nav.dokdisteformidling.exception.functional.Rdist001HentEformidlingforsendelserFunctionalException;
import no.nav.dokdisteformidling.exception.functional.Rdist001HentForsendelseFunctionalException;
import no.nav.dokdisteformidling.exception.functional.Rdist001OppdaterForsendelseFunctionalException;
import no.nav.dokdisteformidling.exception.technical.AbstractDokdisteformidlingTechnicalException;
import no.nav.dokdisteformidling.exception.technical.Rdist001HentEformidlingforsendelserTechnicalException;
import no.nav.dokdisteformidling.exception.technical.Rdist001HentForsendelseTechnicalException;
import no.nav.dokdisteformidling.exception.technical.Rdist001OppdaterForsendelseTechnicalException;
import no.nav.dokdisteformidling.metrics.Monitor;
import org.slf4j.MDC;
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
import org.springframework.web.util.UriComponentsBuilder;

import javax.inject.Inject;
import java.time.Duration;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Slf4j
@Component
public class AdministrerForsendelseConsumer implements AdministrerForsendelse {

	private final String administrerforsendelseV1Url;
	private final RestTemplate restTemplate;

	@Inject
	public AdministrerForsendelseConsumer(@Value("${administrerforsendelse.v1.url}") String administrerforsendelseV1Url,
										  RestTemplateBuilder restTemplateBuilder,
										  final ServiceuserAlias serviceuserAlias) {
		this.administrerforsendelseV1Url = administrerforsendelseV1Url;
		this.restTemplate = restTemplateBuilder
				.setReadTimeout(Duration.ofSeconds(20))
				.setConnectTimeout(Duration.ofSeconds(5))
				.basicAuthentication(serviceuserAlias.getUsername(), serviceuserAlias.getPassword())
				.build();
	}

	@Override
	@Retryable(include = AbstractDokdisteformidlingTechnicalException.class, backoff = @Backoff(delay = RetryConstants.DELAY_SHORT, multiplier = RetryConstants.MULTIPLIER_SHORT))
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

	@Retryable(include = AbstractDokdisteformidlingTechnicalException.class, backoff = @Backoff(delay = RetryConstants.DELAY_SHORT, multiplier = RetryConstants.MULTIPLIER_SHORT))
	@Monitor(value = "dok_consumer", extraTags = {"process", "oppdaterForsendelseStatus"}, histogram = true)
	public void oppdaterForsendelseStatus(String forsendelseId, String forsendelseStatus) {
		String uri = UriComponentsBuilder.fromHttpUrl(administrerforsendelseV1Url)
				.queryParam("forsendelseId", forsendelseId)
				.queryParam("forsendelseStatus", forsendelseStatus)
				.toUriString();
		oppdaterForsendelse(uri);
	}

	@Retryable(include = AbstractDokdisteformidlingTechnicalException.class, backoff = @Backoff(delay = RetryConstants.DELAY_SHORT, multiplier = RetryConstants.MULTIPLIER_SHORT))
	@Monitor(value = "dok_consumer", extraTags = {"process", "oppdaterForsendelseStatusOgKonversasjonsId"}, histogram = true)
	public void oppdaterForsendelseStatusOgKonversasjonsId(String forsendelseId, String forsendelseStatus, String konversasjonsId) {
		String uri = UriComponentsBuilder.fromHttpUrl(administrerforsendelseV1Url)
				.queryParam("forsendelseId", forsendelseId)
				.queryParam("forsendelseStatus", forsendelseStatus)
				.queryParam("konversasjonsId", konversasjonsId)
				.toUriString();
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
	@Retryable(include = AbstractDokdisteformidlingTechnicalException.class, backoff = @Backoff(delay = RetryConstants.DELAY_SHORT, multiplier = RetryConstants.MULTIPLIER_SHORT))
	@Monitor(value = "dok_consumer", extraTags = {"process", "hentEformidlingForsendelser"}, histogram = true)
	public HentEformidlingforsendelserResponseTo hentEformidlingForsendelser() {
		MDC.put("hentEformidlingForsendelser",MdcConstants.CALL_ID);
		try {
			log.info(String.format("%s mottatt kall til å hente forsendelser til trygteretten",MDC.get(MdcConstants.CALL_ID)));
			HttpEntity entity = new HttpEntity<>(createHeaders());
			return restTemplate.exchange(this.administrerforsendelseV1Url + "/henteformidlingforsendelser", HttpMethod.GET, entity, HentEformidlingforsendelserResponseTo.class)
					.getBody();
		} catch (HttpClientErrorException e) {
			throw new Rdist001HentEformidlingforsendelserFunctionalException(
					String.format("Kall mot rdist001 - hentEformidlingForsendelser feilet funksjonelt med statusKode=%s, feilmelding=%s", e
							.getStatusCode(), e.getMessage()), e);
		} catch (HttpServerErrorException e) {
			throw new Rdist001HentEformidlingforsendelserTechnicalException(
					String.format("Kall mot rdist001 - hentEformidlingForsendelser feilet teknisk med statusKode=%s, feilmelding=%s", e
							.getStatusCode(), e.getMessage()), e);
		}
	}

	private HttpHeaders createHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set(MdcConstants.CALL_ID, MDC.get(MdcConstants.CALL_ID));
		return headers;
	}

}
