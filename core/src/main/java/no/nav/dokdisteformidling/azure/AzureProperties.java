package no.nav.dokdisteformidling.azure;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("azure")
public record AzureProperties(@NotEmpty String appClientId,
							  @NotEmpty String appClientSecret,
							  @NotEmpty String openidConfigTokenEndpoint) {
}
