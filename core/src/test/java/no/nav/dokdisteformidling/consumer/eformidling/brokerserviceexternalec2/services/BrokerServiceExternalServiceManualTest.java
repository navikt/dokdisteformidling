package no.nav.dokdisteformidling.consumer.eformidling.brokerserviceexternalec2.services;

import no.nav.dokdisteformidling.certificate.KeyStoreProperties;
import no.nav.dokdisteformidling.config.cxf.BrokerServiceExternalConfig;
import no.nav.dokdisteformidling.config.props.BrokerServiceExternalProperties;
import no.nav.dokdisteformidling.config.props.DpoUserProperties;
import org.apache.cxf.bus.CXFBusFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.FileSystemResource;

import java.net.MalformedURLException;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Disabled("Manuell test")
class BrokerServiceExternalServiceManualTest {
	private KeyStoreProperties keyStoreProperties = new KeyStoreProperties();
	private DpoUserProperties dpoUserProperties = new DpoUserProperties();

	@BeforeEach
	public void setup() throws MalformedURLException {
		// Sett system properties VM options for testen. Ikke putt det i koden.
		//
		// javax.net.ssl.trustStore
		// javax.net.ssl.trustStorePassword
		// virksomhetssertifikat.type
		// virksomhetssertifikat.alias
		// virksomhetssertifikat.password
		// virksomhetssertifikat.path
		System.setProperty("https.proxyHost", "webproxy-utvikler.nav.no");
		System.setProperty("https.proxyPort", "8088");
		System.setProperty("https.nonProxyHosts", "*.155.55.|*.192.168.|*.10.|*.local|*.rtv.gov|*.adeo.no|*.nav.no|*.aetat.no|*.devillo.no|*.oera.no");
		keyStoreProperties.setType(System.getProperty("virksomhetssertifikat.type"));
		keyStoreProperties.setAlias(System.getProperty("virksomhetssertifikat.alias"));
		keyStoreProperties.setPassword(System.getProperty("virksomhetssertifikat.password"));
		keyStoreProperties.setPath(new FileSystemResource(System.getProperty("virksomhetssertifikat.path")));
		dpoUserProperties.setUsername(System.getProperty("dpo.username"));
		dpoUserProperties.setPassword(System.getProperty("dpo.password"));
	}

	@Test
	void shouldTest() throws Exception {
		BrokerServiceExternalProperties brokerServiceExternalProperties = new BrokerServiceExternalProperties();
//		brokerServiceExternalEC2Properties.setEndpointurl("https://www.altinn.no/ServiceEngineExternal/BrokerServiceExternalEC2.svc");
		brokerServiceExternalProperties.setEndpointurl("https://tt02.altinn.no/ServiceEngineExternal/BrokerServiceExternalEC2.svc");
		brokerServiceExternalProperties.setConnecttimeoutms(5000);
		brokerServiceExternalProperties.setReadtimeoutms(30000);

		final BrokerServiceExternalConfig brokerServiceExternalConfig = new BrokerServiceExternalConfig(CXFBusFactory.getDefaultBus());

		BrokerServiceExternalService brokerServiceExternalService = new BrokerServiceExternalService(
				brokerServiceExternalConfig.iBrokerServiceExternal(brokerServiceExternalProperties, dpoUserProperties), dpoUserProperties);
		brokerServiceExternalService.test();
	}
}