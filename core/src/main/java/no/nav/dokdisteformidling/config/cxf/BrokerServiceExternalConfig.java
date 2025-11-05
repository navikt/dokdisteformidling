package no.nav.dokdisteformidling.config.cxf;


import no.altinn.brokerserviceexternal.IBrokerServiceExternal;
import no.nav.dokdisteformidling.config.interceptor.CookiesInInterceptor;
import no.nav.dokdisteformidling.config.interceptor.CookiesOutInterceptor;
import no.nav.dokdisteformidling.config.interceptor.HeaderOutInterceptor;
import no.nav.dokdisteformidling.config.props.BrokerServiceExternalProperties;
import no.nav.dokdisteformidling.config.props.DpoUserProperties;
import org.apache.cxf.Bus;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import static no.altinn.brokerserviceexternal.BrokerServiceExternalSF.CustomBindingIBrokerServiceExternal;
import static no.altinn.brokerserviceexternal.BrokerServiceExternalSF.SERVICE;

@Configuration
@Profile("nais")
public class BrokerServiceExternalConfig extends AbstractCxfEndpointConfig {

	public BrokerServiceExternalConfig(Bus bus, DpoUserProperties dpoUserProperties) {
		super(bus, dpoUserProperties);
	}

	@SuppressWarnings("unchecked")
	@Bean
	public IBrokerServiceExternal iBrokerServiceExternal(BrokerServiceExternalProperties brokerServiceExternalProperties) {
		setWsdlUrl("wsdl/BrokerServiceExternal.wsdl");
		setServiceName(SERVICE);
		setEndpointName(CustomBindingIBrokerServiceExternal);
		setAddress(brokerServiceExternalProperties.getEndpointurl());
		setReceiveTimeout(brokerServiceExternalProperties.getReadtimeoutms());
		setConnectTimeout(brokerServiceExternalProperties.getConnecttimeoutms());

		addInInterceptor(new CookiesInInterceptor());
		addOutInterceptor(new HeaderOutInterceptor());
		addOutInterceptor(new CookiesOutInterceptor());

		IBrokerServiceExternal iBrokerServiceExternalEC2 = createPort(IBrokerServiceExternal.class);
		final Client client = ClientProxy.getClient(iBrokerServiceExternalEC2);
		setRequestContext(client);
		return iBrokerServiceExternalEC2;
	}
}
