package no.nav.dokdisteformidling.azure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

@Data
@Validated
@Configuration
@ConfigurationProperties("azure")
public class AzureConfig {
	@NotEmpty
	private String openidConfigTokenEndpoint;
	@NotEmpty
	private String appClientId;
	@NotEmpty
	private String appClientSecret;
}
