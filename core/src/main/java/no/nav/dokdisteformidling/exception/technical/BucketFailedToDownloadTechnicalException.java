package no.nav.dokdisteformidling.exception.technical;

public class BucketFailedToDownloadTechnicalException extends AbstractDokdisteformidlingTechnicalException {
	public BucketFailedToDownloadTechnicalException(String message) {
		super(message);
	}

	public BucketFailedToDownloadTechnicalException(String message, Throwable cause) {
		super(message, cause);
	}
}
