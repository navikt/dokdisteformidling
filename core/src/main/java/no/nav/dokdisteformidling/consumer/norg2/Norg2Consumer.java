package no.nav.dokdisteformidling.consumer.norg2;

import static java.lang.String.format;
import static no.nav.dokdisteformidling.constants.DomainConstants.APP_NAME;
import static no.nav.dokdisteformidling.constants.MdcConstants.NAV_CALL_ID;
import static no.nav.dokdisteformidling.constants.MdcConstants.NAV_CONSUMER_ID;
import static no.nav.dokdisteformidling.constants.RetryConstants.DELAY_SHORT;

import no.nav.dokdisteformidling.constants.MdcConstants;
import no.nav.dokdisteformidling.exception.functional.Norg2HentEnhetsInfoFunctionalException;
import no.nav.dokdisteformidling.exception.technical.Norg2HentEnhetsInfoTechnicalException;
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
public class Norg2Consumer implements Norg2 {

	private final RestTemplate restTemplate;
	private final String norg2Url;

	public Norg2Consumer(RestTemplateBuilder restTemplateBuilder,
						 @Value("${norg2.api.v1.url}") String norg2Url) {
		this.restTemplate = restTemplateBuilder
				.setReadTimeout(Duration.ofSeconds(20))
				.setConnectTimeout(Duration.ofSeconds(5))
				.build();
		this.norg2Url = norg2Url;
	}

	@Monitor(value = "dok_metric", extraTags = {"process", "hentEnhetsInfo" }, percentiles = {0.5, 0.95}, histogram = true)
	@Retryable(include = Norg2HentEnhetsInfoTechnicalException.class, backoff = @Backoff(delay = DELAY_SHORT))
	public HentEnhetsInfoResponse hentOrgnr(String enhetsNr) {
		try {
			return restTemplate.exchange(norg2Url + "/enhet/" + enhetsNr,
					HttpMethod.GET, new HttpEntity<>(createHeaders()), HentEnhetsInfoResponse.class).getBody();
		} catch (HttpClientErrorException e) {
			throw new Norg2HentEnhetsInfoFunctionalException(format("Funksjonell feil ved kall mot norg2:hentEnhetsInfo (v1/enhet/<enhetsNr>) for enhetsNr=%s. Feilmelding=%s",
					enhetsNr, e.getMessage()), e);
		} catch (HttpServerErrorException e) {
			throw new Norg2HentEnhetsInfoTechnicalException(format("Teknisk feil ved kall mot norg2:hentEnhetsInfo (v1/enhet/<enhetsNr>) for enhetsNr=%s. Feilmelding=%s",
					enhetsNr, e.getMessage()), e);
		}
	}

	private HttpHeaders createHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add(NAV_CONSUMER_ID, APP_NAME);
		headers.add(NAV_CALL_ID, MDC.get(MdcConstants.CALL_ID));
		return headers;
	}
}
