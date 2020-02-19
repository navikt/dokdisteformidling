package no.nav.dokdisteformidling.consumer.eformidling.maskinporten;

import no.nav.dokdisteformidling.exception.functional.AbstractDokdisteformidlingFunctionalException;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public class MaskinportenFunctionalException extends AbstractDokdisteformidlingFunctionalException {
    public MaskinportenFunctionalException(String message, Throwable cause) {
        super(message, cause);
    }
}
