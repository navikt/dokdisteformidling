package no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker;

import no.nav.dokdisteformidling.AppTestUtils;
import no.nav.dokdisteformidling.certificate.AppCertificate;
import no.nav.dokdisteformidling.consumer.eformidling.NavDokument;
import no.nav.dokdisteformidling.consumer.eformidling.NavDokumentpakke;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.exceptions.DokumentpakkingException;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Clock;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static no.nav.dokdisteformidling.AppTestUtils.zipEntries;
import static no.nav.dokdisteformidling.CertTestUtils.itestPemCertificate;
import static no.nav.dokdisteformidling.CertTestUtils.itestVirksomhetssertifikatProperties;
import static no.nav.dokdisteformidling.constants.DomainConstants.DEFAULT_ZONE_ID;
import static no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.EformidlingMessagePackager.EFORMIDLING_ASIC;
import static no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.EformidlingMessagePackager.EFORMIDLING_SBD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
class EformidlingMessagePackagerTest {
	private static final String FIXED_TIME = "2020-01-01T12:00:00Z";
	public static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse(FIXED_TIME), DEFAULT_ZONE_ID);
	private static final String ARKIVMELDING = AppTestUtils.classpathToString("avtaltmelding/arkivmelding.xml");

	private final EformidlingMessagePackager eformidlingMessagePackager = new EformidlingMessagePackager(
			new JacksonConfig().eformidlingObjectMapper(FIXED_CLOCK),
			new StandardBusinessDocumentMapper(FIXED_CLOCK),
			new EformidlingContentPackager());

	@Test
	void shouldPackageEformidlingMessage() throws Exception {
		final NavDokumentpakke navDokumentpakke = NavDokumentpakke.builder()
				.conversationId("1")
				.bestillingsId("2")
				.arkivmelding(NavDokument.fromAvtaltmelding(new ByteArrayInputStream("avtalt".getBytes())))
				.navDokumenter(Collections.singletonList(NavDokument.fromVedlegg("test1.pdf", new ByteArrayInputStream("test1pdf".getBytes()))))
				.build();

		final InputStream inputStream = eformidlingMessagePackager.packageMessage(navDokumentpakke, ARKIVMELDING,
				new AppCertificate(itestVirksomhetssertifikatProperties()),
				itestPemCertificate());

		final List<AppTestUtils.ZipFile> zipEntries = zipEntries(inputStream);
		final AppTestUtils.ZipFile sbdZip = zipEntries.stream().filter(z -> EFORMIDLING_SBD.equals(z.getName())).findFirst().orElseThrow(IllegalStateException::new);
		assertThat(sbdZip.getContentsAsString()).isEqualToIgnoringWhitespace(AppTestUtils.classpathToString("sbd/sbd.json"));
		zipEntries.stream().filter(z -> EFORMIDLING_ASIC.equals(z.getName())).findFirst().orElseThrow(IllegalStateException::new);
	}

	@Test
	void shouldThrowDokumentpakkingExceptionWhenNullArkivmeldingInputStream() {
		final NavDokumentpakke navDokumentpakke = NavDokumentpakke.builder()
				.conversationId("1")
				.bestillingsId("2")
				.arkivmelding(NavDokument.fromAvtaltmelding(null))
				.navDokumenter(Collections.singletonList(NavDokument.fromVedlegg("test1.pdf", new ByteArrayInputStream("test1pdf".getBytes()))))
				.build();

		final DokumentpakkingException dokumentpakkingException = assertThrows(DokumentpakkingException.class, () -> {
			eformidlingMessagePackager.packageMessage(navDokumentpakke, ARKIVMELDING,
					new AppCertificate(itestVirksomhetssertifikatProperties()),
					itestPemCertificate());
		});
		assertThat(dokumentpakkingException.getMessage()).isEqualTo("Klarte ikke lage asic eller kryptere dokumentpakke.");
	}
}