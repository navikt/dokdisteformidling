package no.nav.dokdisteformidling;

import no.nav.dokdisteformidling.azure.AzureProperties;
import no.nav.dokdisteformidling.config.alias.MqGatewayAlias;
import no.nav.dokdisteformidling.config.alias.ServiceuserAlias;
import no.nav.dokdisteformidling.config.props.DokdisteformidlingProperties;
import no.nav.dokdisteformidling.config.props.NaisProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.retry.annotation.EnableRetry;

@EnableRetry
@EnableConfigurationProperties({ServiceuserAlias.class,
		MqGatewayAlias.class,
		DokdisteformidlingProperties.class,
		AzureProperties.class,
		NaisProperties.class
})
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class Application {

	static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
