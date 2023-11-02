package no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker;

import no.nav.dokdisteformidling.AppTestUtils;
import no.nav.dokdisteformidling.certificate.AppCertificate;
import no.nav.dokdisteformidling.consumer.eformidling.NavDokumentpakke;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import static java.util.Arrays.asList;
import static no.nav.dokdisteformidling.AppTestUtils.zipEntries;
import static no.nav.dokdisteformidling.CertTestUtils.itestPemCertificate;
import static no.nav.dokdisteformidling.CertTestUtils.itestPrivateKey;
import static no.nav.dokdisteformidling.CertTestUtils.itestVirksomhetssertifikatProperties;
import static no.nav.dokdisteformidling.consumer.eformidling.NavDokument.fromAvtaltmelding;
import static no.nav.dokdisteformidling.consumer.eformidling.NavDokument.fromVedlegg;
import static org.assertj.core.api.Assertions.assertThat;

class EformidlingContentPackagerTest {

	private static final String ARKIVMELDING_CONTENTS = "arkivmelding";
	private static final String DOKUMENT_1_NAME = "test1.pdf";
	private static final String DOKUMENT_1_CONTENTS = "test1pdf";
	private static final String DOKUMENT_2_NAME = "test2.pdf";
	private static final String DOKUMENT_2_CONTENTS = "test2pdf";

	private final EformidlingContentPackager eformidlingContentPackager = new EformidlingContentPackager();
	private final CmsUtil cmsUtil = new CmsUtil();

	@Test
	void shouldCreateAndEncryptDokumentpakke() throws Exception {
		final NavDokumentpakke navDokumentpakke = NavDokumentpakke.builder()
				.conversationId("1")
				.bestillingsId("2")
				.arkivmelding(fromAvtaltmelding(new ByteArrayInputStream(ARKIVMELDING_CONTENTS.getBytes())))
				.navDokumenter(asList(fromVedlegg(DOKUMENT_1_NAME, new ByteArrayInputStream(DOKUMENT_1_CONTENTS.getBytes())),
						fromVedlegg(DOKUMENT_2_NAME, new ByteArrayInputStream(DOKUMENT_2_CONTENTS.getBytes()))))
				.build();

		final InputStream encryptedAsice = eformidlingContentPackager.packageContent(navDokumentpakke,
				new AppCertificate(itestVirksomhetssertifikatProperties()), itestPemCertificate());

		final InputStream decryptedAsice = cmsUtil.decryptCMSStreamed(encryptedAsice, itestPrivateKey());
		final List<AppTestUtils.ZipFile> asicFiles = zipEntries(IOUtils.toBufferedInputStream(decryptedAsice));
		assertThat(asicFiles).size().isEqualTo(8);
	}
}