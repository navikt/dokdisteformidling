package no.nav.dokdisteformidling.config.props;

import lombok.Data;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@ToString
@Data
@ConfigurationProperties("feature")
@Validated
public class FeatureToggleProperties {
	private boolean usealtinnformidlingstjenesten;
}
