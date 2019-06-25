package no.nav.dokdisteformidling.consumer.saf.graphql;

import static no.nav.dokdisteformidling.constants.RetryConstants.DELAY_SHORT;
import static no.nav.dokdisteformidling.constants.RetryConstants.MAX_ATTEMPTS_SHORT;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokdisteformidling.consumer.saf.journalpost.SafJournalpost;
import no.nav.dokdisteformidling.consumer.saf.journalpost.SafJsonJournalpost;
import no.nav.dokdisteformidling.consumer.sts.StsRestConsumer;
import no.nav.dokdisteformidling.exception.functional.SafJournalpostIkkeFunnetFunctionalException;
import no.nav.dokdisteformidling.exception.functional.SafJournalpostQueryUnauthorizedException;
import no.nav.dokdisteformidling.exception.technical.MarshalGraphqlRequestToJsonTechnicalException;
import no.nav.dokdisteformidling.exception.technical.SafJournalpostQueryTechnicalException;
import no.nav.dokdisteformidling.metrics.Monitor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;
import java.time.Duration;

@Component
@Slf4j
public class SafGraphqlConsumer {

	private static final String OIDC_TOKEN_PREFIX = "Bearer";
	private final RestTemplate restTemplate;
	private final String graphQLurl;
	private final StsRestConsumer stsRestConsumer;

	@Inject
	public SafGraphqlConsumer(RestTemplateBuilder restTemplateBuilder,
							  @Value("${saf.graphql.url}") String graphQLurl,
							  StsRestConsumer stsRestConsumer) {
		this.restTemplate = restTemplateBuilder
				.setReadTimeout(Duration.ofSeconds(20))
				.setConnectTimeout(Duration.ofSeconds(5))
				.build();
		this.graphQLurl = graphQLurl;
		this.stsRestConsumer = stsRestConsumer;
	}

	@Monitor(value = "dok_metric", extraTags = {"process", "safJournalpostquery"}, histogram = true)
	@Retryable(include = SafJournalpostQueryTechnicalException.class, maxAttempts = MAX_ATTEMPTS_SHORT, backoff = @Backoff(delay = DELAY_SHORT))
	public SafJournalpost performQuery(GraphQLRequest graphQLRequest) {

		try {
			ResponseEntity<SafJsonJournalpost> responseEntity = restTemplate.exchange(graphQLurl, HttpMethod.POST, new HttpEntity<>(requestToJson(graphQLRequest), createAuthorizationHeader()), SafJsonJournalpost.class);

			if (responseEntity.getBody() == null || responseEntity.getBody().getData() == null || responseEntity.getBody().getData().getJournalpost() == null) {
				throw new SafJournalpostIkkeFunnetFunctionalException("Ingen journalpost ble funnet");
			}

			return responseEntity.getBody().getJournalpost();

		} catch (HttpClientErrorException e) {
			throw new SafJournalpostQueryUnauthorizedException(String.format("Henting av journalpost feilet med status: %s, feilmelding: %s", e.getStatusCode(), e.getMessage()), e);
		} catch (HttpServerErrorException e) {
			throw new SafJournalpostQueryTechnicalException(String.format("Tjenesten SAF (graphQL) feilet med status: %s, feilmelding: %s", e.getStatusCode(), e.getMessage()), e);
		}
	}

	private HttpHeaders createAuthorizationHeader() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set(HttpHeaders.AUTHORIZATION, OIDC_TOKEN_PREFIX + " " + stsRestConsumer.getOidcToken());
		return headers;
	}

	private String requestToJson(GraphQLRequest graphQLRequest) {
		try {
			return new ObjectMapper().writeValueAsString(graphQLRequest);
		} catch (JsonProcessingException e) {
			throw new MarshalGraphqlRequestToJsonTechnicalException(String.format("Kunne ikke konvertere graphQlRequest til json, feilmelding=%s", e.getMessage()), e);
		}
	}
}
