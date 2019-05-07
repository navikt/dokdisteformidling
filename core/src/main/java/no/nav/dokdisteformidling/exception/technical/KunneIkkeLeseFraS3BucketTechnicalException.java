package no.nav.dokdisteformidling.exception.technical;

/**
 * @author Sigurd Midttun, Visma Consulting
 */
public class KunneIkkeLeseFraS3BucketTechnicalException extends AbstractDokdisteformidlingTechnicalException {

	public KunneIkkeLeseFraS3BucketTechnicalException(String message) {
		super(message);
	}

	public KunneIkkeLeseFraS3BucketTechnicalException(String message, Throwable cause) {
		super(message, cause);
	}
}
