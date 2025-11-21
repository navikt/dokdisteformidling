package no.nav.dokdisteformidling.config.props;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties("dpo")
@Validated
public record DpoUserProperties(
		@NotBlank String username,
		@NotBlank String password
) {
}
