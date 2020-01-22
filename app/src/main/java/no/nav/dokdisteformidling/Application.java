package no.nav.dokdisteformidling;

import no.nav.dokdisteformidling.config.alias.DigitalKontaktinformasjonV1Alias;
import no.nav.dokdisteformidling.config.alias.MqGatewayAlias;
import no.nav.dokdisteformidling.config.alias.ServiceuserAlias;
import no.nav.dokdisteformidling.config.props.MaskinportenProperties;
import no.nav.dokdisteformidling.config.props.ServiceRegistryProperties;
import no.nav.dokdisteformidling.config.props.SrvAppserverProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableRetry
@EnableScheduling
@EnableConfigurationProperties({ServiceuserAlias.class,
		MqGatewayAlias.class,
		SrvAppserverProperties.class,
		DigitalKontaktinformasjonV1Alias.class,
		MaskinportenProperties.class,
		ServiceRegistryProperties.class})
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
