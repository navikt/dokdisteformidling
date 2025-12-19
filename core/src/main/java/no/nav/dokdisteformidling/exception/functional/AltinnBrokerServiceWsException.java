package no.nav.dokdisteformidling.exception.functional;

import no.nav.dokdisteformidling.consumer.eformidling.altinn.to.AltinnReason;

public class AltinnBrokerServiceWsException extends AbstractDokdisteformidlingFunctionalException {
	public AltinnBrokerServiceWsException(String message) {
		super(message);
	}

	public AltinnBrokerServiceWsException(String message, Throwable cause) {
		super(message, cause);
	}

	public AltinnBrokerServiceWsException(String message, AltinnReason altinnReason, Exception e) {
		super(message + " " + altinnReason, e);
	}
}
