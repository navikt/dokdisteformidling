package no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.exceptions;

import no.nav.dokdisteformidling.exception.technical.AbstractDokdisteformidlingTechnicalException;

public class DokumentpakkingException extends AbstractDokdisteformidlingTechnicalException {

    public DokumentpakkingException(String message) {
        super(message);
    }

    public DokumentpakkingException(String message, Throwable cause) {
        super(message, cause);
    }
}
