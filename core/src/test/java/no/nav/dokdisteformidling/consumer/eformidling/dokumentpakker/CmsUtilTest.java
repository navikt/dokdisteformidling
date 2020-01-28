package no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker;

import static no.nav.dokdisteformidling.CertTestUtils.itestVirksomhetssertifikatBase64Properties;
import static org.assertj.core.api.Assertions.assertThat;

import no.nav.dokdisteformidling.CertTestUtils;
import no.nav.dokdisteformidling.certificate.AppCertificate;
import no.nav.dokdisteformidling.certificate.KeyStoreProperties;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.KeyPair;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
class CmsUtilTest {
	@Test
	void shouldEncryptAndDecryptWithCmsWhenKeysGeneratedProgramatically() throws Exception {
		KeyPair keyPair = CertTestUtils.generateKeyPair();
		Certificate certificate = CertTestUtils.generateCertificate(keyPair.getPublic(), keyPair.getPrivate());
		byte[] plaintext = "Text to be encrypted".getBytes();
		ByteArrayOutputStream ciphertext = new ByteArrayOutputStream();

		final CmsUtil cmsUtil = new CmsUtil();
		cmsUtil.createCMSStreamed(new ByteArrayInputStream(plaintext), ciphertext, (X509Certificate) certificate);
		final InputStream recoveredPlaintext = cmsUtil.decryptCMSStreamed(new ByteArrayInputStream(ciphertext.toByteArray()), keyPair.getPrivate());

		assertThat(IOUtils.toByteArray(recoveredPlaintext)).isEqualTo(plaintext);
	}

	@Test
	void shouldEncryptAndDecryptWithCmsWhenB64PKCS12File() throws Exception {
		AppCertificate appCertificate = new AppCertificate(itestVirksomhetssertifikatBase64Properties());

		byte[] plaintext = "Text to be encrypted".getBytes();
		ByteArrayOutputStream ciphertext = new ByteArrayOutputStream();

		final CmsUtil cmsUtil = new CmsUtil();
		cmsUtil.createCMSStreamed(new ByteArrayInputStream(plaintext), ciphertext, appCertificate.getX509Certificate());
		final InputStream recoveredPlaintext = cmsUtil.decryptCMSStreamed(new ByteArrayInputStream(ciphertext.toByteArray()), appCertificate.loadPrivateKey());

		assertThat(IOUtils.toByteArray(recoveredPlaintext)).isEqualTo(plaintext);
	}
}