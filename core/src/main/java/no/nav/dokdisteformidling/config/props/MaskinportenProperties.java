package no.nav.dokdisteformidling.config.props;

import lombok.Data;
import lombok.ToString;
import no.nav.dokdisteformidling.certificate.KeyStoreProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.validation.annotation.Validated;

import java.net.URL;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@ToString
@Data
@ConfigurationProperties("maskinporten")
@Validated
public class MaskinportenProperties {
	private URL url;
	private String audience;
	private String clientid;
	@NestedConfigurationProperty
	private KeyStoreProperties keystore;
}
