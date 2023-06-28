package no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.exceptions;

import no.nav.dokdisteformidling.exception.technical.AbstractDokdisteformidlingTechnicalException;

public class DokumentUnpackingException extends AbstractDokdisteformidlingTechnicalException {

    public DokumentUnpackingException(String message, Throwable cause) {
        super(message, cause);
    }
}