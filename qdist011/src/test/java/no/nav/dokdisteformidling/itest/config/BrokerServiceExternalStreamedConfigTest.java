package no.nav.dokdisteformidling.itest.config;

import no.altinn.brokerserviceexternalstreamed.BrokerServiceExternalStreamedSF;
import no.altinn.brokerserviceexternalstreamed.IBrokerServiceExternalStreamed;
import no.nav.dokdisteformidling.config.cxf.AbstractCxfEndpointConfig;
import no.nav.dokdisteformidling.config.interceptor.ClientCallBackHandler;
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

import java.io.IOException;

@Configuration
@Profile("itest")
public class BrokerServiceExternalStreamedConfigTest extends AbstractCxfEndpointConfig {

    public BrokerServiceExternalStreamedConfigTest(Bus bus) {
        super(bus);
    }

    @Bean
    public IBrokerServiceExternalStreamed iBrokerServiceExternalStreamed(BrokerServiceExternalStreamedProperties brokerServiceExternalStreamedProperties, DpoUserProperties dpoUserProperties) throws IOException {
        setWsdlUrl("wsdl/BrokerServiceExternalStreamedTest.wsdl");
        setServiceName(BrokerServiceExternalStreamedSF.SERVICE);
        setEndpointName(BrokerServiceExternalStreamedSF.CustomBindingIBrokerServiceExternalStreamed);
        setAddress(brokerServiceExternalStreamedProperties.getEndpointurl());
        setReceiveTimeout(brokerServiceExternalStreamedProperties.getReadtimeoutms());
        setConnectTimeout(brokerServiceExternalStreamedProperties.getConnecttimeoutms());

        addInInterceptor(new CookiesInInterceptor());
        addOutInterceptor(new HeaderOutInterceptor());
        addOutInterceptor(new CookiesOutInterceptor());

        IBrokerServiceExternalStreamed iBrokerServiceExternalStreamed = createPort(IBrokerServiceExternalStreamed.class);
        final Client client = ClientProxy.getClient(iBrokerServiceExternalStreamed);
        setRequestContext(client, dpoUserProperties);
        return iBrokerServiceExternalStreamed;
    }

    private void setRequestContext(final Client client, DpoUserProperties dpoUserProperties) {
        client.getRequestContext().put("ws-security.must-understand", Boolean.TRUE);
        client.getRequestContext().put("ws-security.username", dpoUserProperties.getUsername());
        client.getRequestContext().put("ws-security.callback-handler", new ClientCallBackHandler(dpoUserProperties));
        client.getRequestContext().put("org.apache.cxf.message.Message.MAINTAIN_SESSION", Boolean.TRUE);
        client.getRequestContext().put("javax.xml.ws.session.maintain", Boolean.TRUE);
    }

}
