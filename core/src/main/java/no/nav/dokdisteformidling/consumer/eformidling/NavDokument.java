package no.nav.dokdisteformidling.consumer.eformidling;

import lombok.Builder;
import lombok.Value;

import java.io.InputStream;

@Builder
@Value
public class NavDokument {

	private static final String ARKIVMELDING_XML_FILENAME = "arkivmelding.xml";
	public static final String MIMETYPE_XML = "application/xml";
	public static final String MIMETYPE_PDF = "application/pdf";

	String filnavn;
	String mimeType;
	InputStream innhold;

	public static NavDokument fromAvtaltmelding(final InputStream contents) {
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
