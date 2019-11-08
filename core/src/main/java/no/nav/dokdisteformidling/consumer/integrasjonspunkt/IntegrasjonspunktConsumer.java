package no.nav.dokdisteformidling.consumer.integrasjonspunkt;

import static java.lang.String.format;
import static no.nav.dokdisteformidling.constants.RetryConstants.DELAY_SHORT;

import no.nav.dokdisteformidling.exception.functional.IntegrasjonspunktRequestFunctionalException;
import no.nav.dokdisteformidling.exception.technical.IntegrasjonspunktRequestTechnicalException;
import no.nav.dokdisteformidling.metrics.Monitor;
import no.nav.dokdisteformidling.storage.DokdistDokument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.PagedResources;
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
 * @author Heidi Elisabeth Sando, Visma Consulting
 */
@Component
public class IntegrasjonspunktConsumer implements Integrasjonspunkt {

	private final RestTemplate restTemplate;
	private final String integrasjonspunktUrl;
	private final String MESSAGES_OUT_PATH = "/messages/out";

	public IntegrasjonspunktConsumer(RestTemplateBuilder restTemplateBuilder,
									 @Value("${integrasjonspunkt_api_url}") String integrasjonspunktUrl) {
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
			restTemplate.postForObject(this.integrasjonspunktUrl + MESSAGES_OUT_PATH, createMessageRequest, Object.class);
		} catch (HttpClientErrorException e) {
			throw new IntegrasjonspunktRequestFunctionalException(format("Funksjonell feil ved kall mot tjenesten opprettMelding på integrasjonspunktet til Difi. ConversationId=%s. Feilmelding=%s",
					conversationId, e.getResponseBodyAsString()), e);
		} catch (HttpServerErrorException e) {
			throw new IntegrasjonspunktRequestTechnicalException(format("Teknisk feil ved kall mot tjenesten opprettMelding på integrasjonspunktet til Difi. ConversationId=%s  Feilmelding=%s",
					conversationId, e.getResponseBodyAsString()), e);
		}
	}

	@Monitor(value = "dok_metric", extraTags = {"process", "integrasjonspunktLastOppFiler"}, histogram = true, percentiles = {0.5, 0.95})
	@Retryable(include = IntegrasjonspunktRequestTechnicalException.class, backoff = @Backoff(delay = DELAY_SHORT))
	public void lastOppFil(DokdistDokument dokument, String title, String filename, String conversationId) {
		try {
			HttpHeaders headers = createContentDispositionHeader(title, filename);
			restTemplate.exchange(this.integrasjonspunktUrl + MESSAGES_OUT_PATH + "/" + conversationId,
					HttpMethod.PUT, new HttpEntity<>(dokument.getPdf(), headers), Object.class);
		} catch (HttpClientErrorException e) {
			throw new IntegrasjonspunktRequestFunctionalException(format("Funksjonell feil ved kall til lastOppFil " +
					"mot integrasjonspunkt til Difi. Feilmelding=%s", e.getResponseBodyAsString()), e);
		} catch (HttpServerErrorException e) {
			throw new IntegrasjonspunktRequestTechnicalException(format("Teknisk feil ved kall på lastOppFil " +
					"mot integrasjonspunkt til Difi. Feilmelding=%s", e.getResponseBodyAsString()), e);
		}
	}

	@Monitor(value = "dok_metric", extraTags = {"process", "integrasjonspunktSendMelding"}, histogram = true, percentiles = {0.5, 0.95})
	@Retryable(include = IntegrasjonspunktRequestTechnicalException.class, backoff = @Backoff(delay = DELAY_SHORT))
	public void sendMelding(String conversationId) {
		try {
			restTemplate.postForObject(this.integrasjonspunktUrl + MESSAGES_OUT_PATH + "/" + conversationId, null, Object.class);
		} catch (HttpClientErrorException e) {
			throw new IntegrasjonspunktRequestFunctionalException(format("Funksjonell feil ved kall til sendMelding " +
					"mot integrasjonspunkt til Difi. Feilmelding=%s", e.getResponseBodyAsString()), e);
		} catch (HttpServerErrorException e) {
			throw new IntegrasjonspunktRequestTechnicalException(format("Teknisk feil ved kall på sendMelding " +
					"mot integrasjonspunkt til Difi. Feilmelding=%s", e.getResponseBodyAsString()), e);
		}
	}

	@Monitor(value = "dok_metric", extraTags = {"process", "integrasjonspunktGetStatus"}, histogram = true, percentiles = {0.5, 0.95})
	@Retryable(include = IntegrasjonspunktRequestTechnicalException.class, backoff = @Backoff(delay = DELAY_SHORT))
	public String getStatus(String conversationId) {
		try {
			PagedResources<MessageStatus> response = restTemplate.exchange(
					this.integrasjonspunktUrl + "/statuses?conversationId=" + conversationId, HttpMethod.GET,
					new HttpEntity<>(createContentTypeHeader()), new ParameterizedTypeReference<PagedResources<MessageStatus>>() {}).getBody();
			return MessageStatus.findLatestStatus(response.getContent());
		} catch (HttpClientErrorException e) {
			throw new IntegrasjonspunktRequestFunctionalException(String.format("Funksjonell feil ved kall til getStatus " +
					"mot integrasjonspunkt til Difi for conversationId=%s: %s", conversationId, e.getResponseBodyAsString()), e);
		} catch (HttpServerErrorException e) {
			throw new IntegrasjonspunktRequestTechnicalException(String.format("Teknisk feil ved kall til getStatus " +
					"mot integrasjonspunkt til Difi for conversationId=%s: %s", conversationId, e.getResponseBodyAsString()), e);
		}
	}

	private HttpHeaders createContentDispositionHeader(String title, String filename) {
		String contentDispositionHeader = format("attachment; name=%s; filename=%s", title, filename);
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_DISPOSITION, contentDispositionHeader);
		return headers;
	}

	private HttpHeaders createContentTypeHeader() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		return headers;
	}

}
