package no.nav.dokdisteformidling.consumer.eformidling;

import lombok.Builder;
import lombok.Value;

import java.io.InputStream;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Builder
@Value
public class NavDokument {
	private final String filnavn;
	private final String mimeType;
	private final InputStream contents;
}
