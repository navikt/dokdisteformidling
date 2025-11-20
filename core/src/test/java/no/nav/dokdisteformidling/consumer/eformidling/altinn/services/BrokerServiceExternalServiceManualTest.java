package no.nav.dokdisteformidling.consumer.eformidling.altinn.services;

import no.nav.dokdisteformidling.config.cxf.BrokerServiceExternalConfig;
import no.nav.dokdisteformidling.config.props.BrokerServiceExternalProperties;
import no.nav.dokdisteformidling.config.props.DpoUserProperties;
import org.apache.cxf.bus.CXFBusFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled("Manuell test")
class BrokerServiceExternalServiceManualTest {
	private DpoUserProperties dpoUserProperties;

	@BeforeEach
	public void setup() {
		// Sett system properties VM options for testen. Ikke putt det i koden.
		//
		// javax.net.ssl.trustStore
		// javax.net.ssl.trustStorePassword
		// dpo.username
		// dpo.password
		System.setProperty("https.proxyHost", "webproxy-utvikler.nav.no");
		System.setProperty("https.proxyPort", "8088");
		System.setProperty("https.nonProxyHosts", "*.155.55.|*.192.168.|*.10.|*.local|*.rtv.gov|*.adeo.no|*.nav.no|*.aetat.no|*.devillo.no|*.oera.no");

		dpoUserProperties = new DpoUserProperties(System.getProperty("dpo.username"), System.getProperty("dpo.password"));
	}

	@Test
	void shouldTest() {
		BrokerServiceExternalProperties brokerServiceExternalProperties = new BrokerServiceExternalProperties();
		brokerServiceExternalProperties.setEndpointurl("https://www.altinn.no/ServiceEngineExternal/BrokerServiceExternal.svc");
//		brokerServiceExternalProperties.setEndpointurl("https://tt02.altinn.no/ServiceEngineExternal/BrokerServiceExternal.svc");
		brokerServiceExternalProperties.setConnecttimeoutms(5000);
		brokerServiceExternalProperties.setReadtimeoutms(30000);

		final BrokerServiceExternalConfig brokerServiceExternalConfig = new BrokerServiceExternalConfig(CXFBusFactory.getDefaultBus());

		BrokerServiceExternalService brokerServiceExternalService = new BrokerServiceExternalService(
				brokerServiceExternalConfig.iBrokerServiceExternal(brokerServiceExternalProperties, dpoUserProperties));
		brokerServiceExternalService.test();
	}
}