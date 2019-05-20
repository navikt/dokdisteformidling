package no.nav.dokdisteformidling.exception.functional;

/**
 * @author Jarl Øystein Samseth, Visma Consulting
 */
public class DocumentNotFoundInS3FunctionalException extends AbstractDokdisteformidlingFunctionalException { // todo bruk eller kast

	public DocumentNotFoundInS3FunctionalException(String message) {
		super(message);
	}

	public DocumentNotFoundInS3FunctionalException(String message, Throwable cause) {
		super(message, cause);
	}
}
