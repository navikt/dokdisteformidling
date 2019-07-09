package no.nav.dokdisteformidling.consumer.tps;

import static java.lang.String.format;
import static no.nav.dokdisteformidling.constants.DomainConstants.APP_NAME;
import static no.nav.dokdisteformidling.constants.DomainConstants.BEARER_PREFIX;
import static no.nav.dokdisteformidling.constants.MdcConstants.NAV_CALL_ID;
import static no.nav.dokdisteformidling.constants.MdcConstants.NAV_CONSUMER_ID;
import static no.nav.dokdisteformidling.constants.RetryConstants.DELAY_SHORT;

import no.nav.dokdisteformidling.constants.MdcConstants;
import no.nav.dokdisteformidling.consumer.sts.StsRestConsumer;
import no.nav.dokdisteformidling.exception.functional.TpsHentNavnFunctionalException;
import no.nav.dokdisteformidling.exception.technical.TpsHentNavnTechnicalException;
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

import java.time.Duration;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Component
public class TpsConsumer implements Tps {

	private static final String NAV_PERSONIDENT = "Nav-Personident";

	private final RestTemplate restTemplate;
	private final String tpsProxyUrl;
	private final StsRestConsumer stsRestConsumer;

	public TpsConsumer(RestTemplateBuilder restTemplateBuilder,
					   @Value("${tpsproxy.api.url}") String tpsProxyUrl,
					   StsRestConsumer stsRestConsumer) {
		this.restTemplate = restTemplateBuilder
				.setReadTimeout(Duration.ofSeconds(20))
				.setConnectTimeout(Duration.ofSeconds(5))
				.build();
		this.tpsProxyUrl = tpsProxyUrl;
		this.stsRestConsumer = stsRestConsumer;
	}

	@Monitor(value = "dok_metric", extraTags = {"process", "hentNavn"}, histogram = true, percentiles = {0.5, 0.95})
	@Retryable(include = TpsHentNavnTechnicalException.class, backoff = @Backoff(delay = DELAY_SHORT))
	public String hentNavn(String fnr) {
		try {
			final String fnrTrimmed = fnr.trim();
			HttpHeaders headers = createHeaders();
			headers.add(NAV_PERSONIDENT, fnrTrimmed);
			TpsHentNavnResponse response = restTemplate.exchange(tpsProxyUrl + "/v1/navn", HttpMethod.GET, new HttpEntity<>(headers), TpsHentNavnResponse.class)
					.getBody();
			return getFullName(response);
		} catch (HttpClientErrorException e) {
			throw new TpsHentNavnFunctionalException(format("Funkjsonell feil ved kall mot tpsProxy:hentnavn. feilmelding==%s", e
					.getMessage()), e);
		} catch (HttpServerErrorException e) {
			throw new TpsHentNavnTechnicalException(format("Teknisk feil ved kall mot tpsProxy:hentNavn. Feilmelding=%s", e.getMessage()), e);
		}
	}

	private HttpHeaders createHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + stsRestConsumer.getOidcToken());
		headers.add(NAV_CONSUMER_ID, APP_NAME);
		headers.add(NAV_CALL_ID, MDC.get(MdcConstants.CALL_ID));
		return headers;
	}

	private String getFullName(TpsHentNavnResponse tpsHentNavnResponse) {
		return trimString(format("%s %s", trimString(tpsHentNavnResponse.getFornavn()), trimString(tpsHentNavnResponse.getEtternavn())));
	}

	private String trimString(String string) {
		return string == null ? "" : string.trim();
	}
}
