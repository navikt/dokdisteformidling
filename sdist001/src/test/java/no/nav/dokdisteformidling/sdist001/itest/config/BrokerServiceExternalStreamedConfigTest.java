package no.nav.dokdisteformidling.sdist001.itest.config;

import no.altinn.brokerserviceexternalstreamed.IBrokerServiceExternalStreamed;
import no.nav.dokdisteformidling.config.cxf.AbstractCxfEndpointConfig;
import no.nav.dokdisteformidling.config.interceptor.CookiesInInterceptor;
import no.nav.dokdisteformidling.config.interceptor.CookiesOutInterceptor;
import no.nav.dokdisteformidling.config.interceptor.HeaderOutInterceptor;
import no.nav.dokdisteformidling.config.props.BrokerServiceExternalStreamedProperties;
import no.nav.dokdisteformidling.config.props.DpoUserProperties;
import org.apache.cxf.Bus;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import static no.altinn.brokerserviceexternalstreamed.BrokerServiceExternalStreamedSF.CustomBindingIBrokerServiceExternalStreamed;
import static no.altinn.brokerserviceexternalstreamed.BrokerServiceExternalStreamedSF.SERVICE;

@Configuration
@Profile("itest")
public class BrokerServiceExternalStreamedConfigTest extends AbstractCxfEndpointConfig {

	public BrokerServiceExternalStreamedConfigTest(Bus bus, DpoUserProperties dpoUserProperties) {
		super(bus, dpoUserProperties);
	}

	@Bean
	public IBrokerServiceExternalStreamed iBrokerServiceExternalStreamed(BrokerServiceExternalStreamedProperties brokerServiceExternalStreamedProperties, DpoUserProperties dpoUserProperties) {
		setWsdlUrl("wsdl/BrokerServiceExternalStreamedTest.wsdl");
		setServiceName(SERVICE);
		setEndpointName(CustomBindingIBrokerServiceExternalStreamed);
		setAddress(brokerServiceExternalStreamedProperties.getEndpointurl());
		setReceiveTimeout(brokerServiceExternalStreamedProperties.getReadtimeoutms());
		setConnectTimeout(brokerServiceExternalStreamedProperties.getConnecttimeoutms());

		addInInterceptor(new CookiesInInterceptor());
		addOutInterceptor(new HeaderOutInterceptor());
		addOutInterceptor(new CookiesOutInterceptor());

		IBrokerServiceExternalStreamed iBrokerServiceExternalStreamed = createPort(IBrokerServiceExternalStreamed.class);
		final Client client = ClientProxy.getClient(iBrokerServiceExternalStreamed);
		setRequestContext(client);
		return iBrokerServiceExternalStreamed;
	}
}
