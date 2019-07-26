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
import org.apache.http.protocol.HTTP;
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
import java.util.List;

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
	public void opprettForsendelse(IntegrasjonspunktRequestTo integrasjonspunktRequestTo) {
		String forsendelsesBestillingsId = integrasjonspunktRequestTo.standardBusinessDocumentHeader.getDocumentIdentification()
				.getInstanceIdentifier();
		//TODO: Tok bare denne med for å ha noe sporingsinformasjon. Noe annet som skulle være brukt?

		try {
			restTemplate.postForObject(this.integrasjonspunktUrl + "/createMessageUsingPOST", integrasjonspunktRequestTo, HTTP.class);
			//TODO: Skal det være noe respons? Og skal det være HTTP.class?

		} catch (HttpClientErrorException e) {
			throw new IntegrasjonspunktRequestFunctionalException(String.format("Funkjsonell feil ved kall på opprettforsendelse " +
					"mot integrasjonspunkt til Difi for bestillingsId=%s: %s", forsendelsesBestillingsId, e.getMessage()), e);
		} catch (HttpServerErrorException e) {
			throw new IntegrasjonspunktRequestTechnicalException(String.format("Teknisk feil ved kall på opprettforsendelse " +
					"mot integrasjonspunkt til Difi for bestillingsId=%s: %s", forsendelsesBestillingsId, e.getMessage()), e);
		}
	}

	@Monitor(value = "dok_metric", extraTags = {"process", "integrasjonspunktLastOppFiler"}, histogram = true, percentiles = {0.5, 0.95})
	@Retryable(include = IntegrasjonspunktRequestTechnicalException.class, backoff = @Backoff(delay = DELAY_SHORT))
	public void lastOppFiler(List<DokdistDokument> dokumenter, String arkivmeldingXMLString, String conversationId) {
		try {
			HttpHeaders headers = createHeaders();

			String tittle = null;        //TODO: Hvor kommer tittel fra?
			//TODO
			restTemplate.exchange(this.integrasjonspunktUrl + "/uploadFileUsingPut/" + conversationId + "?" + tittle, HttpMethod.PUT, null, HTTP.class);

		} catch (HttpClientErrorException e) {
			throw new IntegrasjonspunktRequestFunctionalException(String.format("Funkjsonell feil ved kall til lastOppFiler " +
					"mot integrasjonspunkt til Difi for conversationId=%s: %s", conversationId, e.getMessage()), e);
		} catch (HttpServerErrorException e) {
			throw new IntegrasjonspunktRequestTechnicalException(String.format("Teknisk feil ved kall på lastOppFiler " +
					"mot integrasjonspunkt til Difi for conversationId=%s: %s", conversationId, e.getMessage()), e);
		}
	}

	@Monitor(value = "dok_metric", extraTags = {"process", "integrasjonspunktSendMelding"}, histogram = true, percentiles = {0.5, 0.95})
	@Retryable(include = IntegrasjonspunktRequestTechnicalException.class, backoff = @Backoff(delay = DELAY_SHORT))
	public void sendMelding(String conversationId) {
		try {
			//TODO
			restTemplate.postForObject(this.integrasjonspunktUrl + "/sendMessageUsingPost/" + conversationId, null, HTTP.class);
		} catch (HttpClientErrorException e) {
			throw new IntegrasjonspunktRequestFunctionalException(String.format("Funkjsonell feil ved kall til lastOppFiler " +
					"mot integrasjonspunkt til Difi for conversationId=%s: %s", conversationId, e.getMessage()), e);
		} catch (HttpServerErrorException e) {
			throw new IntegrasjonspunktRequestTechnicalException(String.format("Teknisk feil ved kall på lastOppFiler " +
					"mot integrasjonspunkt til Difi for conversationId=%s: %s", conversationId, e.getMessage()), e);
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
