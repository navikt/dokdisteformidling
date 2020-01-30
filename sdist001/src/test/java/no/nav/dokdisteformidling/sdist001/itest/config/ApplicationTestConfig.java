package no.nav.dokdisteformidling.sdist001.itest.config;

import static org.mockito.Mockito.mock;

import no.nav.dokdisteformidling.certificate.KeyStoreProperties;
import no.nav.dokdisteformidling.config.alias.DigitalKontaktinformasjonV1Alias;
import no.nav.dokdisteformidling.config.alias.ServiceuserAlias;
import no.nav.dokdisteformidling.config.props.*;
import no.nav.dokdisteformidling.config.sts.STSConfig;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Configuration
@Profile("itest")
@EnableConfigurationProperties({ServiceuserAlias.class, DpoUserProperties.class,
		BrokerServiceExternalProperties.class,
		BrokerServiceExternalStreamedProperties.class,
		KeyStoreProperties.class,
		DigitalKontaktinformasjonV1Alias.class,
		MaskinportenProperties.class,
		ServiceRegistryProperties.class})
@ComponentScan(basePackages = "no.nav.dokdisteformidling")
public class ApplicationTestConfig {

	@Bean
	public STSConfig stsConfig() {
		return mock(STSConfig.class);
	}
}
