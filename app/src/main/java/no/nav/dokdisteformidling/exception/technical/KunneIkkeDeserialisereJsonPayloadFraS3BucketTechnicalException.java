package no.nav.dokdisteformidling.exception.technical;

/**
 * @author Jarl Øystein Samseth, Visma Consulting
 */
public class KunneIkkeDeserialisereJsonPayloadFraS3BucketTechnicalException extends AbstractDokdisteformidlingTechnicalException { // todo bruk eller kast

	public KunneIkkeDeserialisereJsonPayloadFraS3BucketTechnicalException(String message) {
		super(message);
	}

	public KunneIkkeDeserialisereJsonPayloadFraS3BucketTechnicalException(String message, Throwable cause) {
		super(message, cause);
	}
}
