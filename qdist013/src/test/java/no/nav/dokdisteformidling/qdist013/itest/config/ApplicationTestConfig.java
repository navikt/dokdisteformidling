package no.nav.dokdisteformidling.qdist013.itest.config;

import no.nav.dokdisteformidling.CoreConfig;
import no.nav.dokdisteformidling.azure.AzureProperties;
import no.nav.dokdisteformidling.certificate.AppCertificateConfig;
import no.nav.dokdisteformidling.certificate.KeyStoreProperties;
import no.nav.dokdisteformidling.config.alias.MqGatewayAlias;
import no.nav.dokdisteformidling.config.alias.ServiceuserAlias;
import no.nav.dokdisteformidling.config.props.BrokerServiceExternalProperties;
import no.nav.dokdisteformidling.config.props.BrokerServiceExternalStreamedProperties;
import no.nav.dokdisteformidling.config.props.DokdisteformidlingProperties;
import no.nav.dokdisteformidling.config.props.DpoUserProperties;
import no.nav.dokdisteformidling.config.props.MaskinportenProperties;
import no.nav.dokdisteformidling.config.props.NaisProperties;
import no.nav.dokdisteformidling.config.props.ServiceRegistryProperties;
import no.nav.dokdisteformidling.storage.BucketStorage;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.retry.annotation.EnableRetry;

import static org.mockito.Mockito.mock;

@Configuration
@Profile("itest")
@EnableRetry
@EnableConfigurationProperties({ServiceuserAlias.class,
		AzureProperties.class,
		DpoUserProperties.class,
		BrokerServiceExternalProperties.class,
		BrokerServiceExternalStreamedProperties.class,
		KeyStoreProperties.class,
		MqGatewayAlias.class,
		MaskinportenProperties.class,
		ServiceRegistryProperties.class,
		DokdisteformidlingProperties.class,
		NaisProperties.class})
@Import({
		CoreConfig.class,
		JmsItestConfig.class,
		AppCertificateConfig.class,
		BrokerServiceExternalTestConfig.class,
		BrokerServiceExternalStreamedConfigTest.class})
public class ApplicationTestConfig {

	@Bean
	public BucketStorage bucketStorage() {
		return mock(BucketStorage.class);
	}

}
