package no.nav.dokdisteformidling.exception.technical;

public class S3FailedToPutDocumentTechnicalException extends AbstractDokdisteformidlingTechnicalException {
	public S3FailedToPutDocumentTechnicalException(String message, Throwable cause) {
		super(message, cause);
	}

	public S3FailedToPutDocumentTechnicalException(String message) {
		super(message);
	}
}
