package no.nav.dokdisteformidling.consumer.eformidling.serviceregistry;

import lombok.Getter;
import lombok.ToString;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.openssl.PEMParser;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Getter
@ToString(exclude = {"pemCertificate", "x509Certificate"})
public class MottakerInfo {
	private final String pemCertificate;
	private X509Certificate x509Certificate;
	private final String serviceCode;
	private final String serviceEditionCode;

	public MottakerInfo(String pemCertificate, String serviceCode, String serviceEditionCode) {
		this.pemCertificate = pemCertificate;
		this.serviceCode = serviceCode;
		this.serviceEditionCode = serviceEditionCode;
		this.x509Certificate = convertToX509(pemCertificate);
	}

	private X509Certificate convertToX509(final String pemCertificate) {
		PEMParser pemParser = openPEMResource(pemCertificate);
		try {
			final Object certificate = pemParser.readObject();
			if (!(certificate instanceof X509CertificateHolder)) {
				throw new MottakerInfoIkkeFunnetException("PEM data inneholder ikke et X.509 sertifikat.");
			} else {
				return new JcaX509CertificateConverter().setProvider("BC").getCertificate((X509CertificateHolder) certificate);
			}
		} catch (CertificateException e) {
			throw new MottakerInfoIkkeFunnetException("Klarte ikke konvertere PEM data til X.509 sertifikat.");
		} catch (IOException e) {
			throw new MottakerInfoIkkeFunnetException("Klarte ikke lese PEM data.");
		}
	}

	private PEMParser openPEMResource(final String pemCertificate) {
		Reader bufferedReader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(pemCertificate.getBytes())));
		return new PEMParser(bufferedReader);
	}
}
