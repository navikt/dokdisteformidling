package no.nav.dokdisteformidling;

import no.nav.dokdisteformidling.azure.AzureProperties;
import no.nav.dokdisteformidling.config.alias.MqGatewayAlias;
import no.nav.dokdisteformidling.config.alias.ServiceuserAlias;
import no.nav.dokdisteformidling.config.props.DokdisteformidlingProperties;
import no.nav.dokdisteformidling.config.props.NaisProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.resilience.annotation.EnableResilientMethods;

@EnableResilientMethods
@EnableConfigurationProperties({ServiceuserAlias.class,
		MqGatewayAlias.class,
		DokdisteformidlingProperties.class,
		AzureProperties.class,
		NaisProperties.class
})
@SpringBootApplication
public class Application {

	static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
