package no.nav.dokdisteformidling.certificate;

import lombok.Data;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotNull;
import java.security.KeyStore;

/**
 * Kopiert fra https://github.com/difi/move-integrasjonspunkt
 */
@Data
@ConfigurationProperties("virksomhetssertifikat")
@ToString(exclude = "password")
@Validated
public class KeyStoreProperties {
	/**
	 * Type of KeyStore
	 * <p>
	 * Examples: JKS, Windows-MY
	 */
	@NotNull
	private String type = KeyStore.getDefaultType();

	/**
	 * Keystore alias for key.
	 */
	@NotNull
	private String alias;

	/**
	 * Path of jks file.
	 * <p>
	 * May be empty if type = Windows-MY
	 */
	@NotNull
	private Resource path;

	/**
	 * Password of keystore and entry.
	 */
	@NotNull
	private String password = "";

	/**
	 * True if the application should only use the Provider from the
	 * keyStore for crypto operations on the keys from the keystore.
	 */
	private Boolean lockProvider = false;
}
