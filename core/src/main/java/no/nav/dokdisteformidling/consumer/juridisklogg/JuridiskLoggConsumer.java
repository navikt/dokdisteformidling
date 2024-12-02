package no.nav.dokdisteformidling.consumer.juridisklogg;

import no.nav.dokdisteformidling.config.alias.ServiceuserAlias;
import no.nav.dokdisteformidling.exception.functional.LagreJuridiskLoggFunctionalException;
import no.nav.dokdisteformidling.exception.technical.AbstractDokdisteformidlingTechnicalException;
import no.nav.dokdisteformidling.exception.technical.LagreJuridiskLoggTechnicalException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

import static java.lang.String.format;

@Component
public class JuridiskLoggConsumer implements JuridiskLogg {

	private final String juridiskLoggUrl;
	private final RestTemplate restTemplate;

	public JuridiskLoggConsumer(@Value("${LagreJuridiskLogg_Rest_Url}") String juridiskLoggUrl,
								RestTemplateBuilder restTemplateBuilder,
								final ServiceuserAlias serviceuserAlias) {
		this.juridiskLoggUrl = juridiskLoggUrl;
		this.restTemplate = restTemplateBuilder
				.readTimeout(Duration.ofSeconds(20))
				.connectTimeout(Duration.ofSeconds(5))
				.basicAuthentication(serviceuserAlias.getUsername(), serviceuserAlias.getPassword())
				.build();
	}

	@Override
	@Retryable(retryFor = AbstractDokdisteformidlingTechnicalException.class)
	public LoggMeldingResponse lagreJuridiskLogg(final LoggMeldingRequest loggMeldingRequest) {
		try {
			return restTemplate.postForObject(this.juridiskLoggUrl, loggMeldingRequest, LoggMeldingResponse.class);
		} catch (HttpClientErrorException e) {
			throw new LagreJuridiskLoggFunctionalException(format("lagreJuridiskLogg feilet funksjonelt med statusKode=%s. Feilmelding=%s",
					e.getStatusCode(), e.getResponseBodyAsString()), e);
		} catch (HttpServerErrorException e) {
			throw new LagreJuridiskLoggTechnicalException(format("lagreJuridiskLogg feilet teknisk med statusKode=%s. Feilmelding=%s", e
					.getStatusCode(), e.getResponseBodyAsString()), e);
		}
	}
}
