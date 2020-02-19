package no.nav.dokdisteformidling.itest.config;

import static org.mockito.Mockito.mock;

import com.amazonaws.services.s3.AmazonS3;
import no.nav.dokdisteformidling.certificate.KeyStoreProperties;
import no.nav.dokdisteformidling.config.alias.DigitalKontaktinformasjonV1Alias;
import no.nav.dokdisteformidling.config.alias.MqGatewayAlias;
import no.nav.dokdisteformidling.config.alias.ServiceuserAlias;
import no.nav.dokdisteformidling.config.cache.LokalCacheConfig;
import no.nav.dokdisteformidling.config.props.BrokerServiceExternalProperties;
import no.nav.dokdisteformidling.config.props.BrokerServiceExternalStreamedProperties;
import no.nav.dokdisteformidling.config.props.DpoUserProperties;
import no.nav.dokdisteformidling.config.props.MaskinportenProperties;
import no.nav.dokdisteformidling.config.props.ServiceRegistryProperties;
import no.nav.dokdisteformidling.config.props.SrvAppserverProperties;
import no.nav.dokdisteformidling.storage.S3Storage;
import no.nav.dokdisteformidling.storage.Storage;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.retry.annotation.EnableRetry;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Configuration
@Profile("itest")
@EnableRetry
@EnableConfigurationProperties({ServiceuserAlias.class,
		DpoUserProperties.class,
		BrokerServiceExternalProperties.class,
		BrokerServiceExternalStreamedProperties.class,
		KeyStoreProperties.class,
		MqGatewayAlias.class,
		SrvAppserverProperties.class,
		DigitalKontaktinformasjonV1Alias.class,
		MaskinportenProperties.class,
		ServiceRegistryProperties.class})
@Import({JmsItestConfig.class,
		LokalCacheConfig.class,
		STSTestConfig.class,
		BrokerServiceExternalTestConfig.class,
		BrokerServiceExternalStreamedConfigTest.class})
@ComponentScan(basePackages = "no.nav.dokdisteformidling")
public class ApplicationTestConfig {

	@Bean
	public AmazonS3 s3() {
		return mock(AmazonS3.class);
	}

	@Bean
	public Storage storage(AmazonS3 s3) {
		return new S3Storage(s3);
	}

}
