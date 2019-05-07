package no.nav.dokdisteformidling.exception.functional;

/**
 * @author Jarl Øystein Samseth, Visma Consulting
 */
public abstract class AbstractDokdisteformidlingFunctionalException extends RuntimeException {

	public AbstractDokdisteformidlingFunctionalException(String message) {
		super(message);
	}

	public AbstractDokdisteformidlingFunctionalException(String message, Throwable cause) {
		super(message, cause);
	}

}
