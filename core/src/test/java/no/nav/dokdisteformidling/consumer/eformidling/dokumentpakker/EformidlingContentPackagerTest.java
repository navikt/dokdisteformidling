package no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker;

import static no.nav.dokdisteformidling.AppTestUtils.zipFilenames;
import static no.nav.dokdisteformidling.CertTestUtils.itestPemCertificate;
import static no.nav.dokdisteformidling.CertTestUtils.itestPrivateKey;
import static no.nav.dokdisteformidling.CertTestUtils.itestVirksomhetssertifikatProperties;
import static org.assertj.core.api.Assertions.assertThat;

import no.nav.dokdisteformidling.certificate.AppCertificate;
import no.nav.dokdisteformidling.consumer.eformidling.NavDokument;
import no.nav.dokdisteformidling.consumer.eformidling.NavDokumentpakke;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
class EformidlingContentPackagerTest {
	private final EformidlingContentPackager eformidlingContentPackager = new EformidlingContentPackager();
	private final CmsUtil cmsUtil = new CmsUtil();

	@Test
	void shouldCreateAndEncryptDokumentpakke() throws Exception {
		final NavDokumentpakke navDokumentpakke = NavDokumentpakke.builder()
				.conversationId("1")
				.bestillingsId("2")
				.arkivmelding(NavDokument.fromArkivmelding(new ByteArrayInputStream("arkivmelding".getBytes())))
				.navDokumenter(Collections.singletonList(NavDokument.fromVedlegg("test1.pdf", new ByteArrayInputStream("test1pdf".getBytes()))))
				.build();

		final InputStream encryptedAsice = eformidlingContentPackager.packageContent(navDokumentpakke,
				new AppCertificate(itestVirksomhetssertifikatProperties()), itestPemCertificate());

		final InputStream decryptedAsice = cmsUtil.decryptCMSStreamed(encryptedAsice, itestPrivateKey());
		final List<String> asiceFilenames = zipFilenames(IOUtils.toBufferedInputStream(decryptedAsice));
		assertThat(asiceFilenames).size().isEqualTo(7);
	}

}