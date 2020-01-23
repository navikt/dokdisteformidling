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
	private static final String ARKIVMELDING_XML_FILENAME = "arkivmelding.xml";
	public static final String MIMETYPE_XML = "application/xml";
	public static final String MIMETYPE_PDF = "application/pdf";

	private final String filnavn;
	private final String mimeType;
	private final InputStream innhold;

	public static NavDokument fromArkivmelding(final InputStream contents) {
		return NavDokument.builder()
				.filnavn(ARKIVMELDING_XML_FILENAME)
				.mimeType(MIMETYPE_XML)
				.innhold(contents)
				.build();
	}

	public static NavDokument fromVedlegg(final String filnavn, final InputStream contents) {
		return NavDokument.builder()
				.filnavn(filnavn)
				.mimeType(MIMETYPE_PDF)
				.innhold(contents)
				.build();
	}
}
