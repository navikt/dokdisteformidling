package no.nav.dokdisteformidling.exception.technical;


/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class StsRetriveTokenException extends AbstractDokdisteformidlingTechnicalException { // todo bruk eller kast

	public StsRetriveTokenException(String message) {
		super(message);
	}

	public StsRetriveTokenException(String message, Throwable cause) {
		super(message, cause);
	}
}
