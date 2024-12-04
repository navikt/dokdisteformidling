package no.nav.dokdisteformidling.consumer.ereg;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdisteformidling.config.props.DokdisteformidlingProperties;
import no.nav.dokdisteformidling.exception.functional.EregFunctionalException;
import no.nav.dokdisteformidling.exception.technical.EregTechnicalException;
import org.slf4j.MDC;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import static no.nav.dokdisteformidling.constants.DomainConstants.APP_NAME;
import static no.nav.dokdisteformidling.constants.MdcConstants.MDC_CALL_ID;
import static no.nav.dokdisteformidling.constants.NavHeaders.NAV_CALL_ID;
import static no.nav.dokdisteformidling.constants.NavHeaders.NAV_CONSUMER_ID;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Slf4j
@Component
public class EregConsumer {

	private final WebClient webClient;

	public EregConsumer(WebClient webClient,
						DokdisteformidlingProperties dokdisteformidlingProperties) {
		this.webClient = webClient.mutate()
				.baseUrl(dokdisteformidlingProperties.getEndpoints().getEreg().getUrl())
				.defaultHeaders(httpHeaders -> {
					httpHeaders.setContentType(APPLICATION_JSON);
					httpHeaders.set(NAV_CONSUMER_ID, APP_NAME);
					httpHeaders.set(NAV_CALL_ID, MDC.get(MDC_CALL_ID));
				})
				.build();
	}

	@Retryable(retryFor = EregTechnicalException.class)
	public String hentOrganisasjonsnavn(String orgnr) {
		var organisasjonsnavn = webClient.get()
				.uri("/{orgnr}/noekkelinfo", orgnr.trim())
				.retrieve()
				.bodyToMono(EregResponse.class)
				.mapNotNull(EregResponse::navn)
				.mapNotNull(EregResponse.Navn::sammensattnavn)
				.doOnError(throwable -> handleError(orgnr, throwable))
				.block();

		if (isBlank(organisasjonsnavn)) {
			throw new EregFunctionalException("Organisasjonsnavn fra Enhetsregisteret for orgnr=%s mangler.".formatted(orgnr));
		}

		return organisasjonsnavn;
	}

	private void handleError(String orgnr, Throwable error) {
		if (error instanceof WebClientResponseException response && response.getStatusCode().is4xxClientError()) {
			if (NOT_FOUND.equals(response.getStatusCode())) {
				throw new EregFunctionalException("Organisasjon med orgnr=%s ikke funnet i Enhetsregisteret".formatted(orgnr), error);
			} else {
				throw new EregFunctionalException("Kall mot Enhetsregisteret feilet funksjonelt med statuskode=%s for orgnr=%s".formatted(response.getStatusCode(), orgnr), error);
			}
		} else {
			throw new EregTechnicalException("Kall mot Enhetsregisteret feilet teknisk for orgnr=%s".formatted(orgnr), error);
		}
	}

}