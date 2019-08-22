package no.nav.dokdisteformidling.consumer.integrasjonspunkt;

import static java.lang.String.format;
import static no.nav.dokdisteformidling.constants.RetryConstants.DELAY_SHORT;

import no.nav.dokdisteformidling.exception.functional.IntegrasjonspunktRequestFunctionalException;
import no.nav.dokdisteformidling.exception.technical.IntegrasjonspunktRequestTechnicalException;
import no.nav.dokdisteformidling.metrics.Monitor;
import no.nav.dokdisteformidling.storage.DokdistDokument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
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
			throw new IntegrasjonspunktRequestFunctionalException(format("Funksjonell feil ved kall mot tjensten opprettMelding på integrasjonspunktet til Difi. ConversationId=%s. Feilmelding=%s",
					conversationId, e.getMessage()), e);
		} catch (HttpServerErrorException e) {
			throw new IntegrasjonspunktRequestTechnicalException(format("Teknisk feil ved kall opprettMelding på integrasjonspunktet til Difi.  ConversationId=%s  Feilmelding=%s",
					conversationId, e.getMessage()), e);
		}
	}

	@Monitor(value = "dok_metric", extraTags = {"process", "integrasjonspunktLastOppFiler"}, histogram = true, percentiles = {0.5, 0.95})
	@Retryable(include = IntegrasjonspunktRequestTechnicalException.class, backoff = @Backoff(delay = DELAY_SHORT))
	public void lastOppFil(DokdistDokument dokument, String title, String filename, String conversationId) {
		try {
			HttpHeaders headers = createContentDispositionHeader(title, filename);
			restTemplate.exchange(this.integrasjonspunktUrl + "/" + conversationId, HttpMethod.PUT, new HttpEntity<>(dokument.getPdf(), headers), Object.class);
		} catch (HttpClientErrorException e) {
			throw new IntegrasjonspunktRequestFunctionalException(format("Funksjonell feil ved kall til lastOppFil " +
					"mot integrasjonspunkt til Difi. Feilmelding=%s", e.getMessage()), e);
		} catch (HttpServerErrorException e) {
			throw new IntegrasjonspunktRequestTechnicalException(format("Teknisk feil ved kall på lastOppFil " +
					"mot integrasjonspunkt til Difi. Feilmelding=%s", e.getMessage()), e);
		}
	}

	@Monitor(value = "dok_metric", extraTags = {"process", "integrasjonspunktSendMelding"}, histogram = true, percentiles = {0.5, 0.95})
	@Retryable(include = IntegrasjonspunktRequestTechnicalException.class, backoff = @Backoff(delay = DELAY_SHORT))
	public void sendMelding(String conversationId) {
		try {
			restTemplate.postForObject(this.integrasjonspunktUrl + "/" + conversationId, null, Object.class);
		} catch (HttpClientErrorException e) {
			throw new IntegrasjonspunktRequestFunctionalException(format("Funksjonell feil ved kall til sendMelding " +
					"mot integrasjonspunkt til Difi. Feilmelding=%s", e.getMessage()), e);
		} catch (HttpServerErrorException e) {
			throw new IntegrasjonspunktRequestTechnicalException(format("Teknisk feil ved kall på sendMelding " +
					"mot integrasjonspunkt til Difi. Feilmelding=%s", e.getMessage()), e);
		}
	}

	private HttpHeaders createContentDispositionHeader(String title, String filename) {
		String contentDispositionHeader = format("attachment; name=%s; filename=%s", title, filename);
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_DISPOSITION, contentDispositionHeader);
		return headers;
	}

}
