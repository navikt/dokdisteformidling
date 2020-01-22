package no.nav.dokdisteformidling.consumer.eformidling;

import lombok.Value;
import no.nav.dokdisteformidling.consumer.integrasjonspunkt.StandardBusinessDocumentHeader;

import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
public class NavDokumentpakke {
	private final StandardBusinessDocumentHeader standardBusinessDocumentHeader;
	private final String arkivmelding;
	private final List<NavDokument> navDokumenter;
}
