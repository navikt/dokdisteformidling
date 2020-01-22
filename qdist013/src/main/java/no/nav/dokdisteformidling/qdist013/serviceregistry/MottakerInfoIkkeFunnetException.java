package no.nav.dokdisteformidling.qdist013.serviceregistry;

import no.nav.dokdisteformidling.exception.functional.AbstractDokdisteformidlingFunctionalException;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public class MottakerInfoIkkeFunnetException extends AbstractDokdisteformidlingFunctionalException {
	public MottakerInfoIkkeFunnetException(String message) {
		super(message);
	}

	public MottakerInfoIkkeFunnetException(String message, Throwable cause) {
		super(message, cause);
	}
}
