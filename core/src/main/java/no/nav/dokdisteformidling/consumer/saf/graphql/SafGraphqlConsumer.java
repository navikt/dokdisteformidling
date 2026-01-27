package no.nav.dokdisteformidling.consumer.saf.graphql;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdisteformidling.config.props.DokdisteformidlingProperties;
import no.nav.dokdisteformidling.consumer.saf.journalpost.SafJournalpost;
import no.nav.dokdisteformidling.consumer.saf.journalpost.SafJsonJournalpost;
import no.nav.dokdisteformidling.consumer.token.NaisTexasConsumer;
import no.nav.dokdisteformidling.consumer.token.NaisTexasRequestInterceptor;
import no.nav.dokdisteformidling.exception.functional.SafJournalpostIkkeFunnetException;
import no.nav.dokdisteformidling.exception.functional.SafJournalpostQueryUnauthorizedException;
import no.nav.dokdisteformidling.exception.technical.SafJournalpostQueryTechnicalException;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.http.HttpStatusCode;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;

import static no.nav.dokdisteformidling.consumer.token.NaisTexasRequestInterceptor.TARGET_SCOPE;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Component
@Slf4j
public class SafGraphqlConsumer {

	private final RestClient restClient;
	private final String safScope;

	public SafGraphqlConsumer(RestClient.Builder restClientBuilder,
							  NaisTexasConsumer naisTexasConsumer,
							  DokdisteformidlingProperties dokdisteformidlingProperties) {
		this.restClient = restClientBuilder.baseUrl(dokdisteformidlingProperties.getEndpoints().getSaf().getUrl())
				.requestFactory(ClientHttpRequestFactoryBuilder.jdk()
						.withCustomizer(jdkClientHttpRequestFactory ->
								jdkClientHttpRequestFactory.setReadTimeout(Duration.ofSeconds(20)))
						.build())
				.requestInterceptor(new NaisTexasRequestInterceptor(naisTexasConsumer))
				.build();
		this.safScope = dokdisteformidlingProperties.getEndpoints().getSaf().getScope();
	}

	@Retryable(retryFor = SafJournalpostQueryTechnicalException.class)
	public SafJournalpost performQuery(GraphQLRequest graphQLRequest) {
		SafJsonJournalpost safJsonJournalpost = restClient.post()
				.accept(APPLICATION_JSON)
				.attribute(TARGET_SCOPE, safScope)
				.body(graphQLRequest)
				.retrieve()
				.onStatus(HttpStatusCode::is4xxClientError, (_, response) -> {
					throw new SafJournalpostQueryUnauthorizedException("Kall til saf feilet funksjonelt med status=" + response.getStatusCode());
				})
				.onStatus(HttpStatusCode::is5xxServerError, (_, response) -> {
					throw new SafJournalpostQueryTechnicalException("Kall til saf feilet teknisk med status=" + response.getStatusCode());
				})
				.body(SafJsonJournalpost.class);

		if (safJsonJournalpost == null || safJsonJournalpost.getData() == null ||
				safJsonJournalpost.getData().getJournalpost() == null) {
			// Forsøk på nytt. GraphQL endepunktet gir kun httpstatus 200. Verdikjeden forventer at man finner journalpost her.
			// Hvis ikke er dette en teknisk feil, ikke funksjonell feil.
			String journalpostId = (String) graphQLRequest.getVariables().get("queryJournalpostId");
			throw new SafJournalpostIkkeFunnetException("Journalpost med journalpostId=" + journalpostId + " ble ikke funnet i saf.");
		}

		return safJsonJournalpost.getJournalpost();
	}
}
