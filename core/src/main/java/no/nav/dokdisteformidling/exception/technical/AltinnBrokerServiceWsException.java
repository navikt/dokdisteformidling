package no.nav.dokdisteformidling.exception.technical;

import no.nav.dokdisteformidling.consumer.eformidling.brokerserviceexternalec2.to.AltinnReason;

public class AltinnBrokerServiceWsException extends RuntimeException {

    public AltinnBrokerServiceWsException(String message, AltinnReason altinnReason, Exception e) {
        super(message + " " + altinnReason, e);
    }

    public AltinnBrokerServiceWsException(String message, Exception e) {
        super(message, e);
    }
}
