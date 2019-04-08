package no.nav.dokdisteformidling.exception.functional;

/**
 * @author Jarl Øystein Samseth, Visma Consulting
 */
public class ValidationException extends AbstractDokdisteformidlingFunctionalException { // todo bruk eller kast

	public ValidationException(String message) {
		super(message);
	}

	public ValidationException(String message, Throwable cause) {
		super(message, cause);
	}
}
