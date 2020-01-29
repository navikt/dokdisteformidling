package no.nav.dokdisteformidling.config.cxf;


import static no.nav.dokdisteformidling.config.cxf.WssX509PropertyFactory.createWssX509TokenProperties;

import no.altinn.brokerserviceexternaec2.BrokerServiceExternalEC2SF;
import no.altinn.brokerserviceexternaec2.IBrokerServiceExternalEC2;
import no.nav.dokdisteformidling.certificate.KeyStoreProperties;
import no.nav.dokdisteformidling.config.interceptor.BadContextTokenInFaultInterceptor;
import no.nav.dokdisteformidling.config.interceptor.CookiesInInterceptor;
import no.nav.dokdisteformidling.config.interceptor.CookiesOutInterceptor;
import no.nav.dokdisteformidling.config.interceptor.HeaderOutInterceptor;
import no.nav.dokdisteformidling.config.props.BrokerServiceExternalEC2Properties;
import org.apache.cxf.Bus;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.inject.Inject;
import java.io.IOException;

@Configuration
public class BrokerServiceExternalEC2Config extends AbstractCxfEndpointConfig {
	@Inject
	public BrokerServiceExternalEC2Config(Bus bus) {
		super(bus);
	}

	@SuppressWarnings("unchecked")
	@Bean
	public IBrokerServiceExternalEC2 iBrokerServiceExternalEC2(BrokerServiceExternalEC2Properties brokerServiceExternalEC2Properties,
															   KeyStoreProperties keyStoreProperties) throws IOException {
		setWsdlUrl("wsdl/BrokerServiceExternalEC2.wsdl");
		setServiceName(BrokerServiceExternalEC2SF.SERVICE);
		setEndpointName(BrokerServiceExternalEC2SF.CustomBindingIBrokerServiceExternalEC2);
		setAddress(brokerServiceExternalEC2Properties.getEndpointurl());
		setReceiveTimeout(brokerServiceExternalEC2Properties.getReadtimeoutms());
		setConnectTimeout(brokerServiceExternalEC2Properties.getConnecttimeoutms());

		addInInterceptor(new CookiesInInterceptor());
		addOutInterceptor(new CookiesOutInterceptor());
		addOutInterceptor(new HeaderOutInterceptor());
		addInFaultInterceptor(new BadContextTokenInFaultInterceptor());

		IBrokerServiceExternalEC2 iBrokerServiceExternalEC2 = createPort(IBrokerServiceExternalEC2.class);
		final Client client = ClientProxy.getClient(iBrokerServiceExternalEC2);
		setRequestContext(client, keyStoreProperties);
		return iBrokerServiceExternalEC2;
	}

	private void setRequestContext(final Client client, final KeyStoreProperties keyStoreProperties) throws IOException {
		client.getRequestContext().put("security.must-understand", Boolean.TRUE);
		client.getRequestContext().put("org.apache.cxf.message.Message.MAINTAIN_SESSION", Boolean.TRUE);
		client.getRequestContext().put("javax.xml.ws.session.maintain", Boolean.TRUE);
		client.getRequestContext().put("security.cache.issued.token.in.endpoint", Boolean.TRUE);
		client.getRequestContext().put("security.issue.after.failed.renew", Boolean.TRUE);
		client.getRequestContext().put("security.signature.properties", createWssX509TokenProperties(keyStoreProperties));
	}
}
