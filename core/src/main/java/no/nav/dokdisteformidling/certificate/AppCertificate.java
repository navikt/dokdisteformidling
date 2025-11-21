package no.nav.dokdisteformidling.certificate;

import lombok.Getter;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * Class responsible for accessing the keystore for the Integrasjonspunkt.
 * <p>
 * Kopiert fra https://github.com/difi/move-integrasjonspunkt
 */
@Getter
public class AppCertificate {

	private static final String ERR_MISSING_PRIVATE_KEY_OR_PASS = "Problem accessing PrivateKey with alias \"%s\" inadequate access or Password is wrong";
	private static final String ERR_MISSING_PRIVATE_KEY = "No PrivateKey with alias \"%s\" found in the KeyStore";
	private static final String ERR_MISSING_CERTIFICATE = "No AppCertificate with alias \"%s\" found in the KeyStore";
	private static final String ERR_GENERAL = "Unexpected problem occurred when operating KeyStore";

	private final KeyStoreProperties properties;
	private final KeyStoreCredentials credentials;
	private final KeyStore keyStore;
	private final PrivateKey privateKey;
	private final X509Certificate certificate;

	public AppCertificate(KeyStoreProperties properties, KeyStoreCredentials credentials) {
		this.properties = properties;
		this.credentials = credentials;
		try {
			this.keyStore = loadKeyStore(properties, credentials);
			this.privateKey = loadPrivateKey();
			this.certificate = loadX509Certificate();
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	private static KeyStore loadKeyStore(KeyStoreProperties properties, KeyStoreCredentials credentials) throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException {
		String type = credentials.type();
		String password = credentials.password();
		Resource path = new FileSystemResource(properties.key());

		KeyStore keyStore = KeyStore.getInstance(type);
		if ("none".equalsIgnoreCase(path.getFilename())) {
			keyStore.load(null, password.toCharArray());
		} else {
			if (path.getFilename().endsWith(".b64")) {
				keyStore.load(java.util.Base64.getDecoder().wrap(path.getInputStream()), password.toCharArray());
			} else {
				keyStore.load(path.getInputStream(), password.toCharArray());
			}
		}
		return keyStore;
	}

	private PrivateKey loadPrivateKey() throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException {
		PrivateKey privateKey = (PrivateKey) keyStore.getKey(credentials.alias(), credentials.password().toCharArray());
		if (privateKey == null) {
			throw new IllegalStateException(String.format(ERR_MISSING_PRIVATE_KEY, credentials.alias()));
		}
		return privateKey;
	}

	private X509Certificate loadX509Certificate() throws KeyStoreException {
		X509Certificate certificate = (X509Certificate) keyStore.getCertificate(credentials.alias());
		if (certificate == null) {
			throw new IllegalStateException(String.format(ERR_MISSING_CERTIFICATE, credentials.alias()));
		}
		return certificate;
	}

	public X509Certificate getX509Certificate() {
		return certificate;
	}

	public PrivateKey getPrivateKey() {
		return privateKey;
	}
}
