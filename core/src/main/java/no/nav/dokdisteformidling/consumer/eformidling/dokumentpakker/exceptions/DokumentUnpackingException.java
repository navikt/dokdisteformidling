package no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.exceptions;

import no.nav.dokdisteformidling.exception.technical.AbstractDokdisteformidlingTechnicalException;

/**
 * Generelle tekniske feil under utpakking av dokument.
 *
 * @author Mårten Elmgren, Visma Consulting
 */
public class DokumentUnpackingException extends AbstractDokdisteformidlingTechnicalException {
    public DokumentUnpackingException(String message) {
        super(message);
    }

    public DokumentUnpackingException(String message, Throwable cause) {
        super(message, cause);
    }
}