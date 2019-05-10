package no.nav.dokdisteformidling.consumer.dki;

import static no.nav.dokdisteformidling.constants.RetryConstants.DELAY_SHORT;
import static no.nav.dokdisteformidling.constants.RetryConstants.MULTIPLIER_SHORT;

import no.nav.dokdisteformidling.exception.functional.DigitalKontaktinformasjonV1KontaktinformasjonIkkeFunnetFunctionalException;
import no.nav.dokdisteformidling.exception.functional.DigitalKontaktinformasjonV1PersonIkkeFunnetFunctionalException;
import no.nav.dokdisteformidling.exception.functional.DigitalKontaktinformasjonV1SikkerhetsbegrensingFunctionalException;
import no.nav.dokdisteformidling.exception.technical.AbstractDokdisteformidlingTechnicalException;
import no.nav.dokdisteformidling.exception.technical.DigitalKontaktinformasjonV1HentSikkerDigitalPostadresseTechnicalException;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.binding.HentSikkerDigitalPostadresseKontaktinformasjonIkkeFunnet;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.binding.HentSikkerDigitalPostadressePersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.binding.HentSikkerDigitalPostadresseSikkerhetsbegrensing;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.meldinger.HentSikkerDigitalPostadresseRequest;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.meldinger.HentSikkerDigitalPostadresseResponse;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

/**
 * @author Erik Bråten, Visma Consulting.
 */
@Component
public class DigitalKontaktinformasjonV1Consumer implements DigitalKontaktinformasjonV1 {

	private final no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.binding.DigitalKontaktinformasjonV1 digitalKontaktinformasjonV1;
	private final SikkerDigitalKontaktinfoMapper sikkerDigitalKontaktinfoMapper = new SikkerDigitalKontaktinfoMapper();

	public DigitalKontaktinformasjonV1Consumer(no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.binding.DigitalKontaktinformasjonV1 digitalKontaktinformasjonV1) {
		this.digitalKontaktinformasjonV1 = digitalKontaktinformasjonV1;
	}

	@Retryable(include = AbstractDokdisteformidlingTechnicalException.class, backoff = @Backoff(delay = DELAY_SHORT, multiplier = MULTIPLIER_SHORT))
	public HentSikkerDigitalPostadresseResponseTo hentSikkerDigitalPostadresse(final String personident) {
		HentSikkerDigitalPostadresseRequest request = new HentSikkerDigitalPostadresseRequest();
		request.setPersonident(personident);

		try {
			HentSikkerDigitalPostadresseResponse response = digitalKontaktinformasjonV1.hentSikkerDigitalPostadresse(request);
			return sikkerDigitalKontaktinfoMapper.map(response);
		} catch (HentSikkerDigitalPostadresseKontaktinformasjonIkkeFunnet e) {
			throw new DigitalKontaktinformasjonV1KontaktinformasjonIkkeFunnetFunctionalException(
					String.format("Kontaktinformasjon ikke funnet for personident=%s", personident), e);
		} catch (HentSikkerDigitalPostadressePersonIkkeFunnet e) {
			throw new DigitalKontaktinformasjonV1PersonIkkeFunnetFunctionalException(
					String.format("Person ikke funnet for personident=%s", personident), e);
		} catch (HentSikkerDigitalPostadresseSikkerhetsbegrensing e) {
			throw new DigitalKontaktinformasjonV1SikkerhetsbegrensingFunctionalException(
					String.format("Sikkerhetsbegrensing for personident=%s", personident), e);
		} catch (Exception e) {
			throw new DigitalKontaktinformasjonV1HentSikkerDigitalPostadresseTechnicalException(
					String.format("Teknisk feil mot DigitalKontaktinformasjonV1:hentSikkerDigitalPostadresse. personident=%s. Feilmelding=%s",
							personident, e.getMessage()), e);
		}
	}
}
