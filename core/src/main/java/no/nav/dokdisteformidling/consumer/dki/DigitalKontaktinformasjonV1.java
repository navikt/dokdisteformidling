package no.nav.dokdisteformidling.consumer.dki;

/**
 * @author Erik Bråten, Visma Consulting.
 */
public interface DigitalKontaktinformasjonV1 {

	HentSikkerDigitalPostadresseResponseTo hentSikkerDigitalPostadresse(final String personident);
}
