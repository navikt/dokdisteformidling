package no.nav.dokdisteformidling.exception.functional;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
public class SafJournalpostQueryUnauthorizedException extends AbstractDokdisteformidlingFunctionalException {

	public SafJournalpostQueryUnauthorizedException(String message) {
		super(message);
	}

	public SafJournalpostQueryUnauthorizedException(String message, Throwable cause) {
		super(message, cause);
	}
}
