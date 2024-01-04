package no.nav.dokdisteformidling.consumer.pdl;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdisteformidling.config.props.DokdisteformidlingProperties;
import no.nav.dokdisteformidling.exception.functional.PdlFunctionalException;
import no.nav.dokdisteformidling.exception.functional.PersonIkkeFunnetException;
import no.nav.dokdisteformidling.exception.technical.AbstractDokdisteformidlingTechnicalException;
import no.nav.dokdisteformidling.exception.technical.PdlHentPersonTechnicalException;
import org.springframework.http.ProblemDetail;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.HashMap;
import java.util.function.Consumer;

import static java.util.Objects.isNull;
import static no.nav.dokdisteformidling.azure.OAuthEnabledWebClientConfig.CLIENT_REGISTRATION_PDL;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId;

@Slf4j
@Component
public class PdlGraphQLConsumer {

	private static final String PERSON_IKKE_FUNNET_CODE = "not_found";
	// https://pdldocs-navno.msappproxy.net/ekstern/index.html#_dokumenter_hjemmel
	private static final String HEADER_PDL_BEHANDLINGSNUMMER = "behandlingsnummer";
	// https://behandlingskatalog.nais.adeo.no/process/purpose/ARKIVPLEIE/756fd557-b95e-4b20-9de9-6179fb8317e6
	private static final String ARKIVPLEIE_BEHANDLINGSNUMMER = "B315";
	private final MapHentNavnResponse mapHentNavnResponse;
	private final WebClient webClient;

	public PdlGraphQLConsumer(DokdisteformidlingProperties dokdisteformidlingProperties,
							  WebClient webClient) {
		this.webClient = webClient.mutate()
				.baseUrl(dokdisteformidlingProperties.getEndpoints().getPdl().getUrl())
				.defaultHeaders(httpHeaders -> {
					httpHeaders.set(HEADER_PDL_BEHANDLINGSNUMMER, ARKIVPLEIE_BEHANDLINGSNUMMER);
					httpHeaders.setContentType(APPLICATION_JSON);
				})
				.build();
		this.mapHentNavnResponse = new MapHentNavnResponse();
	}

	@Retryable(retryFor = AbstractDokdisteformidlingTechnicalException.class)
	public HentPersonInfo hentNavn(final String ident) {
		return webClient.post()
				.attributes(clientRegistrationId(CLIENT_REGISTRATION_PDL))
				.bodyValue(mapRequest(ident))
				.retrieve()
				.bodyToMono(PdlHentPerson.class)
				.mapNotNull(this::mapPersonInfo)
				.doOnError(handlePdlErrors())
				.block();

	}

	private HentPersonInfo mapPersonInfo(PdlHentPerson response) {
		if (isNull(response.getErrors()) || response.getErrors().isEmpty()) {
			return mapHentNavnResponse.mapNavn(response);
		} else {
			if (PERSON_IKKE_FUNNET_CODE.equals(response.getErrors().get(0).getExtensions().getCode())) {
				throw new PersonIkkeFunnetException("Fant ikke personnavn for person i pdl.");
			}
			throw new PdlFunctionalException("Kunne ikke hente personnavn for person i pdl. " + response.getErrors());
		}
	}

	private PDLRequest mapRequest(final String ident) {
		final HashMap<String, Object> variables = new HashMap<>();
		variables.put("ident", ident);

		return PDLRequest.builder()
				.query(hentPersonnavn)
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

	private Consumer<Throwable> handlePdlErrors() {
		return error -> {
			if (error instanceof WebClientResponseException webException && webException.getStatusCode().is4xxClientError()) {
				ProblemDetail problemDetail = webException.getResponseBodyAs(ProblemDetail.class);
				throw new PdlFunctionalException("Kunne ikke hente person fra pdl. problem=" + problemDetail);
			} else {
				throw new PdlHentPersonTechnicalException("Teknisk feil ved kall mot PDL. Se stacktrace", error);
			}
		};
	}

}