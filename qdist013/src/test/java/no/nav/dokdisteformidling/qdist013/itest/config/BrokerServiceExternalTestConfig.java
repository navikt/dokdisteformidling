package no.nav.dokdisteformidling.qdist013.itest.config;

import no.altinn.brokerserviceexternal.BrokerServiceExternalSF;
import no.altinn.brokerserviceexternal.IBrokerServiceExternal;
import no.nav.dokdisteformidling.config.cxf.AbstractCxfEndpointConfig;
import no.nav.dokdisteformidling.config.interceptor.ClientCallBackHandler;
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


@Configuration
@Profile("itest")
public class BrokerServiceExternalTestConfig extends AbstractCxfEndpointConfig {

    public BrokerServiceExternalTestConfig(Bus bus) {
        super(bus);
    }

    @Bean
    public IBrokerServiceExternal iBrokerServiceExternal(BrokerServiceExternalProperties brokerServiceExternalProperties, DpoUserProperties dpoUserProperties) {
        setWsdlUrl("wsdl/BrokerServiceExternalTest.wsdl");
        setServiceName(BrokerServiceExternalSF.SERVICE);
        setEndpointName(BrokerServiceExternalSF.CustomBindingIBrokerServiceExternal);
        setAddress(brokerServiceExternalProperties.getEndpointurl());
        setReceiveTimeout(brokerServiceExternalProperties.getReadtimeoutms());
        setConnectTimeout(brokerServiceExternalProperties.getConnecttimeoutms());

        addInInterceptor(new CookiesInInterceptor());
        addOutInterceptor(new HeaderOutInterceptor());
        addOutInterceptor(new CookiesOutInterceptor());

        IBrokerServiceExternal iBrokerServiceExternal = createPort(IBrokerServiceExternal.class);
        final Client client = ClientProxy.getClient(iBrokerServiceExternal);
        setRequestContext(client, dpoUserProperties);

        return iBrokerServiceExternal;
    }

    private void setRequestContext(final Client client, DpoUserProperties dpoUserProperties) {
        client.getRequestContext().put("ws-security.must-understand", Boolean.TRUE);
        client.getRequestContext().put("ws-security.username", dpoUserProperties.getUsername());
        client.getRequestContext().put("ws-security.callback-handler", new ClientCallBackHandler(dpoUserProperties));
        client.getRequestContext().put("org.apache.cxf.message.Message.MAINTAIN_SESSION", Boolean.TRUE);
        client.getRequestContext().put("jakarta.xml.ws.session.maintain", Boolean.TRUE);
    }
}
