package no.nav.dokdisteformidling.consumer.eformidling;

import lombok.Builder;
import lombok.Value;
import no.nav.dokdisteformidling.consumer.integrasjonspunkt.StandardBusinessDocumentHeader;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
@Builder
public class NavDokumentpakke {
	private final String conversationId;
	private final String bestillingsId;

	private final NavDokument arkivmelding;
	@Builder.Default
	private final List<NavDokument> navDokumenter = new ArrayList<>();
}
