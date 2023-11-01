package no.nav.dokdisteformidling.consumer.pdl;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdisteformidling.consumer.sts.StsRestConsumer;
import no.nav.dokdisteformidling.exception.functional.PdlFunctionalException;
import no.nav.dokdisteformidling.exception.functional.PersonIkkeFunnetException;
import no.nav.dokdisteformidling.exception.technical.AbstractDokdisteformidlingTechnicalException;
import no.nav.dokdisteformidling.exception.technical.PdlHentPersonTechnicalException;
import no.nav.dokdisteformidling.metrics.Monitor;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.RequestEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Duration;
import java.util.HashMap;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;
import static no.nav.dokdisteformidling.constants.MdcConstants.MDC_CALL_ID;
import static no.nav.dokdisteformidling.constants.NavHeaders.NAV_CALL_ID;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@Component
public class PdlGraphQLConsumer {

	private static final String NAV_CONSUMER_TOKEN = "Nav-Consumer-Token";
	private static final String HEADER_PDL_TEMA = "Tema";
	private static final String PERSON_IKKE_FUNNET_CODE = "not_found";

	private final RestTemplate restTemplate;
	private final StsRestConsumer stsConsumer;
	private final URI pdlUrl;
	private final MapHentNavnResponse mapHentNavnResponse;

	public PdlGraphQLConsumer(RestTemplateBuilder restTemplateBuilder,
							  StsRestConsumer stsConsumer,
							  @Value("${pdl.url}") String pdlUrl) {
		this.restTemplate = restTemplateBuilder
				.setConnectTimeout(Duration.ofSeconds(5L))
				.setReadTimeout(Duration.ofSeconds(15L))
				.build();
		this.stsConsumer = stsConsumer;
		this.pdlUrl = UriComponentsBuilder.fromHttpUrl(pdlUrl).build().toUri();
		this.mapHentNavnResponse = new MapHentNavnResponse();
	}

	@Retryable(include = AbstractDokdisteformidlingTechnicalException.class, maxAttempts = 5, backoff = @Backoff(delay = 200))
	@Monitor(value = "dok_metric", extraTags = {"process", "hentNavn"}, percentiles = {0.5, 0.95}, histogram = true)
	public HentPersonInfo hentNavn(final String ident, final String tema) {
		try {
			RequestEntity<PDLRequest> requestEntity = createRequestEntity()
					.header(HEADER_PDL_TEMA, tema)
					.body(mapRequest(ident, hentPersonnavn));

			final PdlHentPerson response = requireNonNull(restTemplate.exchange(requestEntity, PdlHentPerson.class).getBody());
			if (isNull(response.getErrors()) || response.getErrors().isEmpty()) {
				return mapHentNavnResponse.mapNavn(response);
			} else {
				if (PERSON_IKKE_FUNNET_CODE.equals(response.getErrors().get(0).getExtensions().getCode())) {
					throw new PersonIkkeFunnetException("Fant ikke personnavn for person i pdl.");
				}
				throw new PdlFunctionalException("Kunne ikke hente personnavn for person i pdl. " + response.getErrors());
			}

		} catch (HttpClientErrorException e) {
			throw new PdlFunctionalException("Kunne ikke hente person fra pdl.", e);
		} catch (HttpServerErrorException e) {
			throw new PdlHentPersonTechnicalException("Teknisk feil ved kall mot PDL.", e);
		}
	}

	private RequestEntity.BodyBuilder createRequestEntity() {
		final String serviceUserToken = "Bearer " + stsConsumer.getOidcToken();
		return RequestEntity.post(pdlUrl)
				.accept(APPLICATION_JSON)
				.header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.header(AUTHORIZATION, serviceUserToken)
				.header(NAV_CONSUMER_TOKEN, serviceUserToken)
				.header(NAV_CALL_ID, MDC.get(MDC_CALL_ID));
	}

	private PDLRequest mapRequest(final String ident, String query) {
		final HashMap<String, Object> variables = new HashMap<>();
		variables.put("ident", ident);

		return PDLRequest.builder()
				.query(query)
				.variables(variables)
				.build();
	}

	private static final String hentPersonnavn = """
			query hentPerson($ident: ID!){
			  hentPerson(ident: $ident){
			    navn(historikk: false){
			      fornavn
			      mellomnavn
			      etternavn
			      forkortetNavn
			    }
			    folkeregisteridentifikator(historikk: false){
			      identifikasjonsnummer
			      type
			      status
			    }
			  }
			}
			""";

}