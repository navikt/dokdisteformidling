package no.nav.dokdisteformidling.consumer.saf.graphql;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokdisteformidling.consumer.saf.journalpost.SafJournalpost;
import no.nav.dokdisteformidling.consumer.saf.journalpost.SafJsonJournalpost;
import no.nav.dokdisteformidling.consumer.sts.StsRestConsumer;
import no.nav.dokdisteformidling.exception.functional.SafJournalpostIkkeFunnetException;
import no.nav.dokdisteformidling.exception.functional.SafJournalpostQueryUnauthorizedException;
import no.nav.dokdisteformidling.exception.technical.MarshalGraphqlRequestToJsonTechnicalException;
import no.nav.dokdisteformidling.exception.technical.SafJournalpostQueryTechnicalException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

import static no.nav.dokdisteformidling.constants.DomainConstants.BEARER_PREFIX;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Component
@Slf4j
public class SafGraphqlConsumer {

	private final RestTemplate restTemplate;
	private final String graphQLurl;
	private final StsRestConsumer stsRestConsumer;

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

	@Retryable(retryFor = SafJournalpostQueryTechnicalException.class)
	public SafJournalpost performQuery(GraphQLRequest graphQLRequest) {
		try {
			ResponseEntity<SafJsonJournalpost> responseEntity = restTemplate.exchange(graphQLurl, POST, new HttpEntity<>(requestToJson(graphQLRequest), createAuthorizationHeader()), SafJsonJournalpost.class);

			if (responseEntity.getBody() == null || responseEntity.getBody().getData() == null ||
				responseEntity.getBody().getData().getJournalpost() == null) {
				// Forsøk på nytt. GraphQL endepunktet gir kun httpstatus 200. Verdikjeden forventer at man finner journalpost her.
				// Hvis ikke er dette en teknisk feil, ikke funksjonell feil.
				throw new SafJournalpostIkkeFunnetException("Ingen journalpost ble funnet i saf.");
			}
			return responseEntity.getBody().getJournalpost();
		} catch (HttpClientErrorException e) {
			throw new SafJournalpostQueryUnauthorizedException(String.format("Henting av journalpost feilet med status: %s, feilmelding: %s", e
					.getStatusCode(), e.getMessage()), e);
		} catch (HttpServerErrorException e) {
			throw new SafJournalpostQueryTechnicalException(String.format("Tjenesten SAF (graphQL) feilet med status: %s, feilmelding: %s", e
					.getStatusCode(), e.getMessage()), e);
		}
	}

	private HttpHeaders createAuthorizationHeader() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(APPLICATION_JSON);
		headers.set(AUTHORIZATION, BEARER_PREFIX + stsRestConsumer.getOidcToken());
		return headers;
	}

	private String requestToJson(GraphQLRequest graphQLRequest) {
		try {
			return new ObjectMapper().writeValueAsString(graphQLRequest);
		} catch (JsonProcessingException e) {
			throw new MarshalGraphqlRequestToJsonTechnicalException(String.format("Kunne ikke konvertere graphQlRequest til json, feilmelding=%s", e
					.getMessage()), e);
		}
	}
}
