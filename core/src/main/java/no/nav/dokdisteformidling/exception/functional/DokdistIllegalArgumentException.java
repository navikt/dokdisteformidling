package no.nav.dokdisteformidling.exception.functional;

public class DokdistIllegalArgumentException extends AbstractDokdisteformidlingFunctionalException {

	public DokdistIllegalArgumentException(String message) {
		super(message);
	}

	public DokdistIllegalArgumentException(String message, Throwable cause) {
		super(message, cause);
	}
}
