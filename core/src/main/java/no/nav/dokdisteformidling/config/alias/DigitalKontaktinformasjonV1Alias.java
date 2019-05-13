package no.nav.dokdisteformidling.config.alias;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;

/**
 * @author Erik Bråten, Visma Consulting.
 */
@Getter
@Setter
@ToString
@ConfigurationProperties("virksomhet.digitalkontakinformasjon.v1")
@Validated
public class DigitalKontaktinformasjonV1Alias {
	@NotEmpty
	private String endpointurl;
	@Min(1)
	private int readtimeoutms;
	@Min(1)
	private int connecttimeoutms;
}
