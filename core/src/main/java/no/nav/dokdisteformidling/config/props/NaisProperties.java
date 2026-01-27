package no.nav.dokdisteformidling.config.props;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("nais")
public record NaisProperties(@NotBlank String tokenEndpoint) {
}
