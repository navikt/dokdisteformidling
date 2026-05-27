package no.nav.dokdisteformidling.qdist013.itest.config;

import no.nav.dokdisteformidling.CoreConfig;
import no.nav.dokdisteformidling.azure.AzureProperties;
import no.nav.dokdisteformidling.config.alias.MqGatewayAlias;
import no.nav.dokdisteformidling.config.alias.ServiceuserAlias;
import no.nav.dokdisteformidling.config.props.DokdisteformidlingProperties;
import no.nav.dokdisteformidling.config.props.NaisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.resilience.annotation.EnableResilientMethods;

@Configuration
@Profile("itest")
@EnableResilientMethods
@EnableConfigurationProperties({ServiceuserAlias.class,
		AzureProperties.class,
		MqGatewayAlias.class,
		DokdisteformidlingProperties.class,
		NaisProperties.class})
@Import({
		CoreConfig.class,
		JmsItestConfig.class})
public class ApplicationTestConfig {
}
