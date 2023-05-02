package no.nav.dokdisteformidling.exception.technical;

import no.nav.dokdisteformidling.consumer.eformidling.altinn.to.AltinnReason;

public class AltinnBrokerServiceWsException extends AbstractDokdisteformidlingTechnicalException {

    public AltinnBrokerServiceWsException(String message, AltinnReason altinnReason, Exception e) {
        super(message + " " + altinnReason, e);
    }
}
