package no.nav.dokdisteformidling.config.cxf;


import no.altinn.brokerserviceexternal.BrokerServiceExternalSF;
import no.altinn.brokerserviceexternal.IBrokerServiceExternal;
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

import javax.inject.Inject;

@Configuration
@Profile("nais")
public class BrokerServiceExternalConfig extends AbstractCxfEndpointConfig {
    @Inject
    public BrokerServiceExternalConfig(Bus bus) {
        super(bus);
    }

    @SuppressWarnings("unchecked")
    @Bean
    public IBrokerServiceExternal iBrokerServiceExternal(BrokerServiceExternalProperties brokerServiceExternalProperties,
                                                         DpoUserProperties dpoUserProperties) {
        setWsdlUrl("wsdl/BrokerServiceExternal.wsdl");
        setServiceName(BrokerServiceExternalSF.SERVICE);
        setEndpointName(BrokerServiceExternalSF.CustomBindingIBrokerServiceExternal);
        setAddress(brokerServiceExternalProperties.getEndpointurl());
        setReceiveTimeout(brokerServiceExternalProperties.getReadtimeoutms());
        setConnectTimeout(brokerServiceExternalProperties.getConnecttimeoutms());

        addInInterceptor(new CookiesInInterceptor());
        addOutInterceptor(new HeaderOutInterceptor());
        addOutInterceptor(new CookiesOutInterceptor());

        IBrokerServiceExternal iBrokerServiceExternalEC2 = createPort(IBrokerServiceExternal.class);
        final Client client = ClientProxy.getClient(iBrokerServiceExternalEC2);
        setRequestContext(client, dpoUserProperties);
        return iBrokerServiceExternalEC2;
    }

    private void setRequestContext(final Client client, DpoUserProperties dpoUserProperties) {
        client.getRequestContext().put("ws-security.must-understand", Boolean.TRUE);
        client.getRequestContext().put("ws-security.username", dpoUserProperties.getUsername());
        client.getRequestContext().put("ws-security.callback-handler", new ClientCallBackHandler(dpoUserProperties));
        client.getRequestContext().put("org.apache.cxf.message.Message.MAINTAIN_SESSION", Boolean.TRUE);
        client.getRequestContext().put("javax.xml.ws.session.maintain", Boolean.TRUE);
    }
}
