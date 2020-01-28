package no.nav.dokdisteformidling.certificate;

import static no.nav.dokdisteformidling.CertTestUtils.itestVirksomhetssertifikatBase64Properties;
import static no.nav.dokdisteformidling.CertTestUtils.itestVirksomhetssertifikatProperties;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
class AppCertificateTest {
	@Test
	void shouldLoadPKCS12KeyStore() {
		AppCertificate appCertificate = new AppCertificate(itestVirksomhetssertifikatProperties());
		assertNotNull(appCertificate.getX509Certificate());
	}

	@Test
	void shouldLoadPKCS12KeyStoreAsBase64() {
		AppCertificate appCertificate = new AppCertificate(itestVirksomhetssertifikatBase64Properties());
		assertNotNull(appCertificate.getX509Certificate());
	}
}