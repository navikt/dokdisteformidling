package no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker;

import no.nav.dokdisteformidling.AppTestUtils;
import no.nav.dokdisteformidling.CertTestUtils;
import no.nav.dokdisteformidling.certificate.AppCertificate;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static no.nav.dokdisteformidling.AppTestUtils.zipEntries;
import static no.nav.dokdisteformidling.consumer.eformidling.NavDokument.fromAvtaltmelding;
import static no.nav.dokdisteformidling.consumer.eformidling.NavDokument.fromVedlegg;
import static org.assertj.core.api.Assertions.assertThat;

class AsiceCreatorTest {

	private static final String ARKIVMELDING_NAME = "arkivmelding.xml";
	private static final String AVTALTMELDING_CONTENTS = "avtalt";
	private static final String DOKUMENT_1_NAME = "test1.pdf";
	private static final String DOKUMENT_1_CONTENTS = "test1pdf";
	private static final String DOKUMENT_2_NAME = "test2.pdf";
	private static final String DOKUMENT_2_CONTENTS = "test2pdf";

	private final AsiceCreator asiceCreator = new AsiceCreator();

	@Test
	void shouldCreateAndSignAsice() throws Exception {
		final OutputStream asiceStreamed = asiceCreator.createAsiceStreamed(fromAvtaltmelding(new ByteArrayInputStream(AVTALTMELDING_CONTENTS.getBytes())),
				Stream.of(fromVedlegg(DOKUMENT_1_NAME, new ByteArrayInputStream(DOKUMENT_1_CONTENTS.getBytes())),
						fromVedlegg(DOKUMENT_2_NAME, new ByteArrayInputStream(DOKUMENT_2_CONTENTS.getBytes()))),
				new AppCertificate(CertTestUtils.itestVirksomhetssertifikatProperties()));

		final ByteArrayInputStream asice = new ByteArrayInputStream(((ByteArrayOutputStream) asiceStreamed).toByteArray());

		final List<AppTestUtils.ZipFile> zipEntries = zipEntries(IOUtils.toBufferedInputStream(asice));
		assertThat(zipEntries).size().isEqualTo(8);
		assertThat(zipEntries).extracting(AppTestUtils.ZipFile::getName).containsAll(
				Arrays.asList("mimetype",
						"manifest.xml",
						"arkivmelding.xml",
						"test1.pdf",
						"META-INF/ASiCManifest.xml",
						"META-INF/manifest.xml"));
		assertFileContents(zipEntries, ARKIVMELDING_NAME, AVTALTMELDING_CONTENTS);
		assertFileContents(zipEntries, DOKUMENT_1_NAME, DOKUMENT_1_CONTENTS);
		assertFileContents(zipEntries, DOKUMENT_2_NAME, DOKUMENT_2_CONTENTS);
	}

	private void assertFileContents(List<AppTestUtils.ZipFile> zipEntries, String filename, String expectedFileContents) {
		final AppTestUtils.ZipFile arkivmeldingXml = zipEntries.stream()
				.filter(z -> filename.equals(z.getName()))
				.findFirst()
				.orElseThrow(IllegalStateException::new);
		assertThat(arkivmeldingXml.getContentsAsString()).isEqualTo(expectedFileContents);
	}
}