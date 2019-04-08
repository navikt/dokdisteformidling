package no.nav.dokdisteformidling.nais.selftest;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public class ApplicationNotReadyException extends RuntimeException {
	public ApplicationNotReadyException(String message, Throwable cause) {
		super(message, cause);
	}
}