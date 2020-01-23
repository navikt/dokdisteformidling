package no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker;

import static no.nav.dokdisteformidling.AppTestUtils.zipFilenames;
import static org.assertj.core.api.Assertions.assertThat;

import no.nav.dokdisteformidling.CertTestUtils;
import no.nav.dokdisteformidling.certificate.AppCertificate;
import no.nav.dokdisteformidling.consumer.eformidling.NavDokument;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
class AsiceCreatorTest {
	private final AsiceCreator asiceCreator = new AsiceCreator();

	@Test
	void shouldCreateAndSignAsice() throws Exception {
		final OutputStream asiceStreamed = asiceCreator.createAsiceStreamed(NavDokument.fromArkivmelding(new ByteArrayInputStream("arkivmelding".getBytes())),
				Stream.of(NavDokument.fromVedlegg("test1.pdf", new ByteArrayInputStream("vedlegg".getBytes()))),
				new AppCertificate(CertTestUtils.itestVirksomhetssertifikatProperties()));

		final ByteArrayInputStream asice = new ByteArrayInputStream(((ByteArrayOutputStream) asiceStreamed).toByteArray());

		final List<String> filenames = zipFilenames(IOUtils.toBufferedInputStream(asice));
		assertThat(filenames).size().isEqualTo(7);
		assertThat(filenames).containsAll(
				Arrays.asList("mimetype",
						"manifest.xml",
						"arkivmelding.xml",
						"test1.pdf",
						"META-INF/asicmanifest.xml",
						"META-INF/manifest.xml")
		);
	}
}