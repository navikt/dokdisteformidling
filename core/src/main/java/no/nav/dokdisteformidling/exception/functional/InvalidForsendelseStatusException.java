package no.nav.dokdisteformidling.exception.functional;


/**
 * @author Heidi Elisabeth Sando, Visma Consulting
 */

public class InvalidForsendelseStatusException extends AbstractDokdisteformidlingFunctionalException{

	public InvalidForsendelseStatusException(String message) {
		super(message);
	}

	public InvalidForsendelseStatusException(String message, Throwable cause) {
		super(message, cause);
	}

}
