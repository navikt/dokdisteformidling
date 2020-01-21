package no.nav.dokdisteformidling.certificate;

import lombok.Data;
import lombok.ToString;
import org.springframework.core.io.Resource;

import javax.validation.constraints.NotNull;
import java.security.KeyStore;

/**
 * Kopiert fra https://github.com/difi/move-integrasjonspunkt
 */
@Data
@ToString(exclude = "password")
public class KeyStoreProperties {

	/**
	 * Type of KeyStore
	 * <p>
	 * Examples: JKS, Windows-MY
	 */
	private String type = KeyStore.getDefaultType();

	/**
	 * Keystore alias for key.
	 */
	private String alias;

	/**
	 * Path of jks file.
	 * <p>
	 * May be empty if type = Windows-MY
	 */
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
