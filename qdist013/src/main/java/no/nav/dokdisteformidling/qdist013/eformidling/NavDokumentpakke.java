package no.nav.dokdisteformidling.qdist013.eformidling;

import lombok.Value;
import no.nav.dokdisteformidling.storage.DokdistDokument;

import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
public class NavDokumentpakke {
	// StandardBusinessDocumentHeader
	private final String arkivmelding;
	private final List<NavDokument> navDokumenter;
}
