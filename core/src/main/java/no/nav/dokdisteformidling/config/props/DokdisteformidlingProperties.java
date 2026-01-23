package no.nav.dokdisteformidling.config.props;

import jakarta.validation.Valid;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Data
@ConfigurationProperties("dokdisteformidling")
public class DokdisteformidlingProperties {

	@Valid
	private final Endpoints endpoints = new Endpoints();

	@Data
	@Validated
	public static class Endpoints {
		@NotNull
		private AzureEndpoint dokdistadmin;

		@NotNull
		private AzureEndpoint pdl;

		@NotNull
		private AzureEndpoint saf;

		@NotNull
		private Endpoint ereg;
	}

	@Data
	@Validated
	public static class Endpoint {
		@NotEmpty
		private String url;
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
