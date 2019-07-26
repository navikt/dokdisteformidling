package no.nav.dokdisteformidling.consumer.integrasjonspunkt;

import static no.nav.dokdisteformidling.constants.DomainConstants.APP_NAME;
import static no.nav.dokdisteformidling.constants.MdcConstants.CALL_ID;
import static no.nav.dokdisteformidling.constants.RetryConstants.DELAY_SHORT;

import no.nav.dokdisteformidling.consumer.sts.StsRestConsumer;
import no.nav.dokdisteformidling.exception.functional.IntegrasjonspunktRequestFunctionalException;
import no.nav.dokdisteformidling.exception.technical.IntegrasjonspunktRequestTechnicalException;
import no.nav.dokdisteformidling.metrics.Monitor;
import org.apache.http.protocol.HTTP;
import org.jboss.logging.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * @author Heidi Elisabeth Sando, Visma Consulting
 */
@Component
public class IntegrasjonspunktConsumer implements Integrasjonspunkt {

	private final RestTemplate restTemplate;
	private final String integrasjonspunktUrl;
	private final StsRestConsumer stsRestConsumer;

	//Todo: Ligger inne i Sigurds PR!
	private static final String NAV_CONSUMER_ID = "Nav-Consumer-Id";
	private static final String NAV_CALL_ID = "Nav-Call-Id";
	private static final String BEARER_PREFIX = "Bearer ";

	public IntegrasjonspunktConsumer(RestTemplateBuilder restTemplateBuilder,
									 @Value("${integrasjonspunkt.api.url}") String integrasjonspunktUrl,
									 StsRestConsumer stsRestConsumer) {
		this.restTemplate = restTemplateBuilder
				.setReadTimeout(Duration.ofSeconds(20))
				.setConnectTimeout(Duration.ofSeconds(5))
				.build();
		this.integrasjonspunktUrl = integrasjonspunktUrl;
		this.stsRestConsumer = stsRestConsumer;
	}

	@Monitor(value = "dok_metric", extraTags = {"process", "integrasjonspunktOpprettForsendelse"}, histogram = true, percentiles = {0.5, 0.95})
	@Retryable(include = IntegrasjonspunktRequestTechnicalException.class, backoff = @Backoff(delay = DELAY_SHORT))
	public void opprettForsendelse(IntegrasjonspunktRequestTo integrasjonspunktRequestTo) {
		String forsendelsesBestillingsId = integrasjonspunktRequestTo.standardBusinessDocumentHeader.getDocumentIdentification()
				.getInstanceIdentifier();
		//TODO: Tok bare denne med for å ha noe sporingsinformasjon. Noe annet som skulle være brukt?

		try {
			HttpHeaders headers = createHeaders();
			restTemplate.postForObject(this.integrasjonspunktUrl, integrasjonspunktRequestTo, HTTP.class, new HttpEntity<>(headers));
			//TODO: Skal det være noe respons?

		} catch (HttpClientErrorException e) {
			throw new IntegrasjonspunktRequestFunctionalException(String.format("Funkjsonell feil ved kall på opprettforsendelse " +
					"mot integrasjonspunkt til Difi for bestillingsId=%s: %s", forsendelsesBestillingsId, e.getMessage()), e);
		} catch (HttpServerErrorException e) {
			throw new IntegrasjonspunktRequestTechnicalException(String.format("Teknisk feil ved kall på opprettforsendelse " +
					"mot integrasjonspunkt til Difi for bestillingsId=%s: %s", forsendelsesBestillingsId, e.getMessage()), e);
		}
	}

	//Todo: Hva trengs av headere når det skal til DIFI?
	private HttpHeaders createHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + stsRestConsumer.getOidcToken());
		headers.add(NAV_CONSUMER_ID, APP_NAME);
		headers.add(NAV_CALL_ID, (String) MDC.get(CALL_ID));
		return headers;
	}
}
