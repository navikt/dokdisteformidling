package no.nav.dokdisteformidling.config.props;

import lombok.Data;
import lombok.ToString;
import no.nav.dokdisteformidling.certificate.KeyStoreProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import java.net.URL;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@ToString
@Data
@ConfigurationProperties("maskinporten")
@Validated
public class MaskinportenProperties {
	@NotNull
	private URL url;
	@NotNull
	private String audience;
	@NotNull
	private String clientid;
}
