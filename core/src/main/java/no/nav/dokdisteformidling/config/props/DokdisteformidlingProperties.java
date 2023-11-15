package no.nav.dokdisteformidling.config.props;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Data
@ConfigurationProperties("dokdisteformidling")
public class DokdisteformidlingProperties {

	private final Endpoints endpoints = new Endpoints();

	@Data
	@Validated
	public static class Endpoints {
		@NotNull
		private AzureEndpoint dokdistadmin;
	}

	@Data
	@Validated
	public static class AzureEndpoint {
		@NotEmpty
		private String url;
		@NotEmpty
		private String scope;
	}
}
