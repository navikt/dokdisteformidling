package no.nav.dokdisteformidling.consumer.ereg;

import no.nav.dokdisteformidling.exception.functional.EregHentNoekkelinfoFunctionalException;
import no.nav.dokdisteformidling.exception.technical.EregHentNoekkelinfoTechnicalException;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

import static java.lang.String.format;
import static no.nav.dokdisteformidling.constants.DomainConstants.APP_NAME;
import static no.nav.dokdisteformidling.constants.MdcConstants.MDC_CALL_ID;
import static no.nav.dokdisteformidling.constants.NavHeaders.NAV_CALL_ID;
import static no.nav.dokdisteformidling.constants.NavHeaders.NAV_CONSUMER_ID;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Component
public class EregConsumer implements Ereg {

	private final RestTemplate restTemplate;
	private final String eregApiUrl;

	public EregConsumer(RestTemplateBuilder restTemplateBuilder,
						@Value("${ereg.api.url}") String eregApiUrl) {
		this.restTemplate = restTemplateBuilder
				.setReadTimeout(Duration.ofSeconds(20))
				.setConnectTimeout(Duration.ofSeconds(5))
				.build();
		this.eregApiUrl = eregApiUrl;
	}

	@Retryable(retryFor = EregHentNoekkelinfoTechnicalException.class)
	public String hentNavn(String orgnr) {
		try {
			final String orgnrTrimmed = orgnr.trim();
			HttpHeaders headers = createHeaders();

			EregHentNoekkelInfoResponse response = restTemplate.exchange(eregApiUrl + "/v1/organisasjon/" + orgnrTrimmed + "/noekkelinfo",
					GET, new HttpEntity<>(headers), EregHentNoekkelInfoResponse.class).getBody();
			validerRespons(response, orgnrTrimmed);

			return getFullName(response.getNavn());
		} catch (HttpClientErrorException e) {
			throw new EregHentNoekkelinfoFunctionalException(format("Funksjonell feil ved kall mot ereg:hentNoekkelinfo for organisasjonsnummer=%s. feilmelding=%s",
					orgnr, e.getMessage()), e);
		} catch (HttpServerErrorException e) {
			throw new EregHentNoekkelinfoTechnicalException(format("Teknisk feil ved kall mot ereg:hentNoekkelinfo for organisasjonsnummer=%s. Feilmelding=%s",
					orgnr, e.getMessage()), e);
		}
	}

	private HttpHeaders createHeaders() {
		HttpHeaders headers = new HttpHeaders();

		headers.setContentType(APPLICATION_JSON);
		headers.add(NAV_CONSUMER_ID, APP_NAME);
		headers.add(NAV_CALL_ID, MDC.get(MDC_CALL_ID));

		return headers;
	}

	private void validerRespons(EregHentNoekkelInfoResponse eregHentNoekkelInfoResponse, String orgnr) {
		if (eregHentNoekkelInfoResponse == null) {
			throw new EregHentNoekkelinfoFunctionalException(format("Fikk ingen respons fra ereg:hentNoekkelinfo for organisasjonsnummer=%s.", orgnr));
		} else if (eregHentNoekkelInfoResponse.getNavn() == null) {
			throw new EregHentNoekkelinfoFunctionalException(format("Respons fra ereg:hentNoekkelinfo for organisasjonsnummer=%s mangler navn", orgnr));
		}
	}

	private String getFullName(EregHentNoekkelInfoResponse.Navn navn) {
		return trimString(format("%s %s %s %s %s", trimString(navn.getNavnelinje1()), trimString(navn.getNavnelinje2()),
				trimString(navn.getNavnelinje3()), trimString(navn.getNavnelinje4()), trimString(navn.getNavnelinje5())));
	}

	private String trimString(String string) {
		return string == null ? "" : string.trim();
	}
}
