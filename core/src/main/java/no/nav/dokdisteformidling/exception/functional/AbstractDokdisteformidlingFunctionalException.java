package no.nav.dokdisteformidling.exception.functional;

public abstract class AbstractDokdisteformidlingFunctionalException extends RuntimeException {

	public AbstractDokdisteformidlingFunctionalException(String message) {
		super(message);
	}

	public AbstractDokdisteformidlingFunctionalException(String message, Throwable cause) {
		super(message, cause);
	}

}
