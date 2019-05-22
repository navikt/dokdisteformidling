package no.nav.dokdisteformidling.exception.functional;

public class SafJournalpostQueryUnauthorizedException extends AbstractDokdisteformidlingFunctionalException {

	public SafJournalpostQueryUnauthorizedException(String message) {
		super(message);
	}

	public SafJournalpostQueryUnauthorizedException(String message, Throwable cause) {
		super(message, cause);
	}
}
