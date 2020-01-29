package no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker;

import no.nav.dokdisteformidling.exception.technical.AbstractDokdisteformidlingTechnicalException;

/**
 * Generelle tekniske feil under dokumentpakking.
 *
 * @author Joakim Bjørnstad, Jbit AS
 */
public class DokumentpakkingException extends AbstractDokdisteformidlingTechnicalException {
	public DokumentpakkingException(String message) {
		super(message);
	}

	public DokumentpakkingException(String message, Throwable cause) {
		super(message, cause);
	}
}
