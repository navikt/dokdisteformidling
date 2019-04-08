package no.nav.dokdisteformidling.exception.functional;

/**
 * @author Jarl Øystein Samseth, Visma Consulting
 */
public class DokumentIkkeFunnetIS3Exception extends AbstractDokdisteformidlingFunctionalException { // todo bruk eller kast

	public DokumentIkkeFunnetIS3Exception(String message) {
		super(message);
	}

	public DokumentIkkeFunnetIS3Exception(String message, Throwable cause) {
		super(message, cause);
	}
}
