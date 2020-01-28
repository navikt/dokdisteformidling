package no.nav.dokdisteformidling;

import no.nav.dokdisteformidling.certificate.KeyStoreProperties;
import no.nav.dokdisteformidling.config.alias.*;
import no.nav.dokdisteformidling.config.props.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableRetry
@EnableScheduling
@EnableConfigurationProperties({ServiceuserAlias.class, DpoUserProperties.class,
		BrokerServiceExternalEC2Properties.class,
		BrokerServiceExternalECStreamedProperties.class,
		KeyStoreProperties.class,
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
