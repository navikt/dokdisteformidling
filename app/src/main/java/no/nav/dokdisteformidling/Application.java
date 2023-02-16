package no.nav.dokdisteformidling;

import no.nav.dokdisteformidling.certificate.KeyStoreProperties;
import no.nav.dokdisteformidling.config.alias.MqGatewayAlias;
import no.nav.dokdisteformidling.config.alias.ServiceuserAlias;
import no.nav.dokdisteformidling.config.props.BrokerServiceExternalProperties;
import no.nav.dokdisteformidling.config.props.BrokerServiceExternalStreamedProperties;
import no.nav.dokdisteformidling.config.props.DokdisteformidlingProperties;
import no.nav.dokdisteformidling.config.props.DokdistmellomlagerProperties;
import no.nav.dokdisteformidling.config.props.DpoUserProperties;
import no.nav.dokdisteformidling.config.props.MaskinportenProperties;
import no.nav.dokdisteformidling.config.props.ServiceRegistryProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

import static java.lang.System.getenv;
import static java.lang.System.setProperty;

@SpringBootApplication
@EnableRetry
@EnableScheduling
@EnableConfigurationProperties({ServiceuserAlias.class,
		DpoUserProperties.class,
		BrokerServiceExternalProperties.class,
		BrokerServiceExternalStreamedProperties.class,
		KeyStoreProperties.class,
		MqGatewayAlias.class,
		MaskinportenProperties.class,
		ServiceRegistryProperties.class,
		DokdistmellomlagerProperties.class,
		DokdisteformidlingProperties.class})
public class Application {

	public static void main(String[] args) {
		setProperty("javax.net.ssl.keyStorePassword", getenv("DOKDISTEFORMIDLING_KEYSTORE_PASSWORD"));
		SpringApplication.run(Application.class, args);
	}
}
