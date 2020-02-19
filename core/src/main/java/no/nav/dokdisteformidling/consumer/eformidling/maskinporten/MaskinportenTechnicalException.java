package no.nav.dokdisteformidling.consumer.eformidling.maskinporten;

import no.nav.dokdisteformidling.exception.technical.AbstractDokdisteformidlingTechnicalException;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public class MaskinportenTechnicalException extends AbstractDokdisteformidlingTechnicalException {
    public MaskinportenTechnicalException(String message, Throwable cause) {
        super(message, cause);
    }
}
