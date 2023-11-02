package no.nav.dokdisteformidling.config.props;

import lombok.Data;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import java.net.URL;

@ToString
@Data
@ConfigurationProperties("serviceregistry")
@Validated
public class ServiceRegistryProperties {
	@NotNull
	private URL url;
}
