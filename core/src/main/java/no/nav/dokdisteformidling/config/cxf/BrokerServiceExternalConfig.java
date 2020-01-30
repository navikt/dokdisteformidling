package no.nav.dokdisteformidling.config.cxf;


import no.altinn.brokerserviceexternal.BrokerServiceExternalSF;
import no.altinn.brokerserviceexternal.IBrokerServiceExternal;
import no.nav.dokdisteformidling.config.interceptor.ClientCallBackHandler;
import no.nav.dokdisteformidling.config.interceptor.HeaderOutInterceptor;
import no.nav.dokdisteformidling.config.props.BrokerServiceExternalProperties;
import no.nav.dokdisteformidling.config.props.DpoUserProperties;
import org.apache.cxf.Bus;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;
import org.apache.wss4j.dom.WSConstants;
import org.apache.wss4j.dom.handler.WSHandlerConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.inject.Inject;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class BrokerServiceExternalConfig extends AbstractCxfEndpointConfig {
    @Inject
    public BrokerServiceExternalConfig(Bus bus) {
        super(bus);
    }

    @SuppressWarnings("unchecked")
    @Bean
    public IBrokerServiceExternal iBrokerServiceExternal(BrokerServiceExternalProperties brokerServiceExternalProperties,
                                                         DpoUserProperties dpoUserProperties) throws IOException {
        setWsdlUrl("wsdl/BrokerServiceExternal.wsdl");
        setServiceName(BrokerServiceExternalSF.SERVICE);
        setEndpointName(BrokerServiceExternalSF.CustomBindingIBrokerServiceExternal);
        setAddress(brokerServiceExternalProperties.getEndpointurl());
        setReceiveTimeout(brokerServiceExternalProperties.getReadtimeoutms());
        setConnectTimeout(brokerServiceExternalProperties.getConnecttimeoutms());

        addOutInterceptor(new HeaderOutInterceptor());
        addOutInterceptor(getWss4JOutInterceptor(dpoUserProperties));

        IBrokerServiceExternal iBrokerServiceExternalEC2 = createPort(IBrokerServiceExternal.class);
        final Client client = ClientProxy.getClient(iBrokerServiceExternalEC2);
        setRequestContext(client);
        return iBrokerServiceExternalEC2;
    }

    private void setRequestContext(final Client client) throws IOException {
        client.getRequestContext().put("security.must-understand", Boolean.TRUE);
        client.getRequestContext().put("org.apache.cxf.message.Message.MAINTAIN_SESSION", Boolean.TRUE);
        client.getRequestContext().put("javax.xml.ws.session.maintain", Boolean.TRUE);
    }

    private ClientCallBackHandler getClientCallBackHandler(DpoUserProperties dpoUserProperties) {
        return new ClientCallBackHandler(dpoUserProperties);
    }

    private WSS4JOutInterceptor getWss4JOutInterceptor(DpoUserProperties dpoUserProperties) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(WSHandlerConstants.ACTION, WSHandlerConstants.USERNAME_TOKEN);
        properties.put(WSHandlerConstants.PASSWORD_TYPE, WSConstants.PW_TEXT);
        properties.put(WSHandlerConstants.USER, dpoUserProperties.getPassword());
        properties.put(WSHandlerConstants.PW_CALLBACK_REF, getClientCallBackHandler(dpoUserProperties));

        return new WSS4JOutInterceptor(properties);
    }
}
