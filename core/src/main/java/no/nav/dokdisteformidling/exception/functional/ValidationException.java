package no.nav.dokdisteformidling.exception.functional;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Jarl Øystein Samseth, Visma Consulting
 */

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class ValidationException extends AbstractDokdisteformidlingFunctionalException {

	public ValidationException(String message) {
		super(message);
	}

	public ValidationException(String message, Throwable cause) {
		super(message, cause);
	}
}
