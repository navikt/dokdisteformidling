package no.nav.dokdisteformidling.config.props;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@ToString
@ConfigurationProperties("dpo")
@Validated
public class DpoUserProperties {
    @NotBlank
    private String username;
    @NotBlank
    private String password;
}
