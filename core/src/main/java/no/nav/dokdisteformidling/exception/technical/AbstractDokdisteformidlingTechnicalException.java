package no.nav.dokdisteformidling.exception.technical;

public abstract class AbstractDokdisteformidlingTechnicalException extends RuntimeException {

	public AbstractDokdisteformidlingTechnicalException(String message) {
		super(message);
	}

	public AbstractDokdisteformidlingTechnicalException(String message, Throwable cause) {
		super(message, cause);
	}
}
