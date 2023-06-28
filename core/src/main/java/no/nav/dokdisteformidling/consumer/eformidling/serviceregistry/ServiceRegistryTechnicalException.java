package no.nav.dokdisteformidling.consumer.eformidling.serviceregistry;

import no.nav.dokdisteformidling.exception.technical.AbstractDokdisteformidlingTechnicalException;

public class ServiceRegistryTechnicalException extends AbstractDokdisteformidlingTechnicalException {

    public ServiceRegistryTechnicalException(String message, Throwable cause) {
        super(message, cause);
    }
}
