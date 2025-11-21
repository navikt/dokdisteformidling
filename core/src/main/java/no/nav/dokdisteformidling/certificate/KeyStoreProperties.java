package no.nav.dokdisteformidling.certificate;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.ToString;
import no.nav.dok.validators.Exists;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotNull;
import java.security.KeyStore;

/**
 * Kopiert fra https://github.com/difi/move-integrasjonspunkt
 */
@ConfigurationProperties("klageinstans.virksomhetssertifikat")
@Validated
public record KeyStoreProperties (
		@Exists @NotNull
		String key,
		@Exists @NotNull
		String credentials
) {
}
