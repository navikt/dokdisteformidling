package no.nav.dokdisteformidling.consumer.integrasjonspunkt;

import static no.nav.dokdisteformidling.constants.DomainConstants.APP_NAME;
import static no.nav.dokdisteformidling.constants.MdcConstants.NAV_CALL_ID;
import static no.nav.dokdisteformidling.constants.MdcConstants.NAV_CONSUMER_ID;
import static no.nav.dokdisteformidling.constants.RetryConstants.DELAY_SHORT;

import no.nav.dokdisteformidling.constants.MdcConstants;
import no.nav.dokdisteformidling.exception.functional.IntegrasjonspunktRequestFunctionalException;
import no.nav.dokdisteformidling.exception.technical.IntegrasjonspunktRequestTechnicalException;
import no.nav.dokdisteformidling.metrics.Monitor;
import no.nav.dokdisteformidling.storage.DokdistDokument;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
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
 * @author Heidi Elisabeth Sando, Visma Consulting
 */
@Component
public class IntegrasjonspunktConsumer implements Integrasjonspunkt {

	private final RestTemplate restTemplate;
	private final String integrasjonspunktUrl;

	public IntegrasjonspunktConsumer(RestTemplateBuilder restTemplateBuilder,
									 @Value("${integrasjonspunkt.api.url}") String integrasjonspunktUrl) {
		this.restTemplate = restTemplateBuilder
				.setReadTimeout(Duration.ofSeconds(20))
				.setConnectTimeout(Duration.ofSeconds(5))
				.build();
		this.integrasjonspunktUrl = integrasjonspunktUrl;
	}

	@Monitor(value = "dok_metric", extraTags = {"process", "integrasjonspunktOpprettForsendelse"}, histogram = true, percentiles = {0.5, 0.95})
	@Retryable(include = IntegrasjonspunktRequestTechnicalException.class, backoff = @Backoff(delay = DELAY_SHORT))
	public void opprettMelding(CreateMessageRequest createMessageRequest, String conversationId) {
		try {
			restTemplate.postForObject(this.integrasjonspunktUrl, createMessageRequest, Object.class);
		} catch (HttpClientErrorException e) {
			throw new IntegrasjonspunktRequestFunctionalException(String.format("Funkjsonell feil ved kall mot tjensten opprettMelding på integrasjonspunktet til Difi. ConversationId=%s. Feilmelding=%s",
					conversationId, e.getMessage()), e);
		} catch (HttpServerErrorException e) {
			throw new IntegrasjonspunktRequestTechnicalException(String.format("Teknisk feil ved kall opprettMelding på integrasjonspunktet til Difi.  ConversationId=%s  Feilmelding=%s",
					conversationId, e.getMessage()), e);
		}
	}

	@Monitor(value = "dok_metric", extraTags = {"process", "integrasjonspunktLastOppFiler"}, histogram = true, percentiles = {0.5, 0.95})
	@Retryable(include = IntegrasjonspunktRequestTechnicalException.class, backoff = @Backoff(delay = DELAY_SHORT))
	public void lastOppFil(DokdistDokument dokument, String conversationId) {
		try {
			HttpHeaders headers = createHeaders();

			//TODO: Request inn skal være en MultipartFile. Vi må se nærmere på dette api'et
			restTemplate.exchange(this.integrasjonspunktUrl + "/" + conversationId, HttpMethod.PUT, null, Object.class);
		} catch (HttpClientErrorException e) {
			throw new IntegrasjonspunktRequestFunctionalException(String.format("Funkjsonell feil ved kall til lastOppFil " +
					"mot integrasjonspunkt til Difi for conversationId=%s. Feilmelding=%s", conversationId, e.getMessage()), e);
		} catch (HttpServerErrorException e) {
			throw new IntegrasjonspunktRequestTechnicalException(String.format("Teknisk feil ved kall på lastOppFil " +
					"mot integrasjonspunkt til Difi for conversationId=%s. Feilmelding=%s", conversationId, e.getMessage()), e);
		}
	}

	@Monitor(value = "dok_metric", extraTags = {"process", "integrasjonspunktSendMelding"}, histogram = true, percentiles = {0.5, 0.95})
	@Retryable(include = IntegrasjonspunktRequestTechnicalException.class, backoff = @Backoff(delay = DELAY_SHORT))
	public void sendMelding(String conversationId) {
		try {
			restTemplate.postForObject(this.integrasjonspunktUrl + "/" + conversationId, null, Object.class);
		} catch (HttpClientErrorException e) {
			throw new IntegrasjonspunktRequestFunctionalException(String.format("Funkjsonell feil ved kall til sendMelding " +
					"mot integrasjonspunkt til Difi for conversationId=%s. Feilmelding=%s", conversationId, e.getMessage()), e);
		} catch (HttpServerErrorException e) {
			throw new IntegrasjonspunktRequestTechnicalException(String.format("Teknisk feil ved kall på sendMelding " +
					"mot integrasjonspunkt til Difi for conversationId=%s. Feilmelding=%s", conversationId, e.getMessage()), e);
		}
	}

	//TODO: Hvilke headere skal med her? {Content-disposition} og {content-type}?
	private HttpHeaders createHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add(NAV_CONSUMER_ID, APP_NAME);
		headers.add(NAV_CALL_ID, MDC.get(MdcConstants.CALL_ID));
		return headers;
	}
}
