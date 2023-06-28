package no.nav.dokdisteformidling.consumer.eformidling.serviceregistry;

import no.nav.dokdisteformidling.exception.functional.AbstractDokdisteformidlingFunctionalException;

public class MottakerInfoIkkeFunnetException extends AbstractDokdisteformidlingFunctionalException {

	public MottakerInfoIkkeFunnetException(String message) {
		super(message);
	}

	public MottakerInfoIkkeFunnetException(String message, Throwable cause) {
		super(message, cause);
	}
}
