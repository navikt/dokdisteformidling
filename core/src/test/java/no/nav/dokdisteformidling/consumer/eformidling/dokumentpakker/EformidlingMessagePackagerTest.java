package no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker;

import no.nav.dokdisteformidling.certificate.AppCertificate;
import no.nav.dokdisteformidling.certificate.KeyStoreProperties;
import no.nav.dokdisteformidling.consumer.eformidling.NavDokument;
import no.nav.dokdisteformidling.consumer.eformidling.NavDokumentpakke;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.openssl.PEMParser;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.FileSystemResource;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.security.cert.X509Certificate;
import java.util.Collections;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Disabled("Manuell test")
class EformidlingMessagePackagerTest {
	public static final String SELF_SIGNED_MOTTAKER_PEM = "self_signed_mottaker.pem";
	private final EformidlingMessagePackager eformidlingMessagePackager = new EformidlingMessagePackager();

	@Test
	void shouldCreateEncryptedDokumentpakke() throws Exception {
		final NavDokumentpakke navDokumentpakke = new NavDokumentpakke(null,"arkivmelding", Collections.singletonList(NavDokument.builder()
				.filnavn("test1.pdf")
				.mimeType("application/pdf")
				.contents(new ByteArrayInputStream("dokument".getBytes()))
				.build()));

		final KeyStoreProperties keyStoreProperties = new KeyStoreProperties();
		keyStoreProperties.setType(System.getProperty("virksomhetssertifikat.type"));
		keyStoreProperties.setAlias(System.getProperty("virksomhetssertifikat.alias"));
		keyStoreProperties.setPassword(System.getProperty("virksomhetssertifikat.password"));
		keyStoreProperties.setPath(new FileSystemResource(System.getProperty("virksomhetssertifikat.path")));
		final InputStream encryptedAsice = eformidlingMessagePackager.createEformidlingMessage(navDokumentpakke, new AppCertificate(keyStoreProperties), getPemCert());
		IOUtils.copy(encryptedAsice, new FileOutputStream("C:\\test\\asice.zip"));
	}

	private X509Certificate getPemCert() throws Exception {
		final X509Certificate cert;
		PEMParser pemRd = openPEMResource(SELF_SIGNED_MOTTAKER_PEM);
		Object o;

		if ((o = pemRd.readObject()) != null) {
			if (!(o instanceof X509CertificateHolder)) {
				throw new RuntimeException();
			} else {
				cert = new JcaX509CertificateConverter().setProvider("BC")
						.getCertificate((X509CertificateHolder) o);
			}
		} else {
			cert = null;
		}
		return cert;
	}

	private PEMParser openPEMResource(String fileName) {
		InputStream res = getClass().getClassLoader().getResourceAsStream(fileName);
		Reader fRd = new BufferedReader(new InputStreamReader(res));
		return new PEMParser(fRd);
	}
}