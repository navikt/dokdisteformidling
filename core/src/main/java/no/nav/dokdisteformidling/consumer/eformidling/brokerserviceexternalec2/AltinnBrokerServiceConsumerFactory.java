package no.nav.dokdisteformidling.consumer.eformidling.brokerserviceexternalec2;

import no.altinn.brokerserviceexternaec2.BrokerServiceExternalEC2SF;
import no.altinn.brokerserviceexternaec2.IBrokerServiceExternalEC2;
import no.altinn.brokerserviceexternalec2streamed.BrokerServiceExternalEC2StreamedSF;
import no.altinn.brokerserviceexternalec2streamed.IBrokerServiceExternalEC2Streamed;
import no.nav.dokdisteformidling.certificate.SecurityCredential;
import no.nav.dokdisteformidling.config.interceptor.*;
import no.nav.dokdisteformidling.config.props.BrokerServiceExternalEC2Properties;
import no.nav.dokdisteformidling.config.props.BrokerServiceExternalECStreamedProperties;
import no.nav.dokdisteformidling.config.props.DpoUserProperties;
import org.apache.cxf.Bus;
import org.apache.cxf.bus.CXFBusFactory;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;
import org.apache.wss4j.dom.WSConstants;
import org.apache.wss4j.dom.handler.WSHandlerConstants;

import javax.xml.ws.BindingProvider;
import java.util.HashMap;
import java.util.Map;


public class AltinnBrokerServiceConsumerFactory {

    private final BrokerServiceExternalEC2Properties brokerServiceExternalEC2Properties;
    private final DpoUserProperties dpoUserProperties;
    private final BrokerServiceExternalECStreamedProperties streamedProperties;

    public AltinnBrokerServiceConsumerFactory(BrokerServiceExternalEC2Properties brokerServiceExternalEC2Properties,
                                              DpoUserProperties dpoUserProperties, BrokerServiceExternalECStreamedProperties streamedProperties) {
        this.brokerServiceExternalEC2Properties = brokerServiceExternalEC2Properties;
        this.dpoUserProperties = dpoUserProperties;
        this.streamedProperties = streamedProperties;
        addInterceptors();
    }

    public IBrokerServiceExternalEC2 getBrokerServiceClient(SecurityCredential credential) {
        BrokerServiceExternalEC2SF brokerService = new BrokerServiceExternalEC2SF();
        IBrokerServiceExternalEC2 port = brokerService.getCustomBindingIBrokerServiceExternalEC2();
        BindingProvider bindingProvider = (BindingProvider) port;
        bindingProvider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, brokerServiceExternalEC2Properties.getEndpointurl());
        Client client = ClientProxy.getClient(port);
        setRequestContext(client, credential);
        return port;

    }

    public IBrokerServiceExternalEC2Streamed getBrokerServiceExternalEC2Streamed(SecurityCredential credential) {
        BrokerServiceExternalEC2StreamedSF brokerServiceExternalEC2StreamedSF = new BrokerServiceExternalEC2StreamedSF();
        IBrokerServiceExternalEC2Streamed port = brokerServiceExternalEC2StreamedSF.getCustomBindingIBrokerServiceExternalEC2Streamed();
        BindingProvider bindingProvider = (BindingProvider) port;
        bindingProvider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, streamedProperties.getEndpointurl());
        Client client = ClientProxy.getClient(port);
        setRequestContext(client, credential);
        return port;
    }

    private void setRequestContext(Client client, SecurityCredential credential) {
        client.getRequestContext().put("security.must-understand", Boolean.TRUE);
        client.getRequestContext().put("org.apache.cxf.message.Message.MAINTAIN_SESSION", Boolean.TRUE);
        client.getRequestContext().put("javax.xml.ws.session.maintain", Boolean.TRUE);
        client.getRequestContext().put("security.cache.issued.token.in.endpoint", Boolean.TRUE);
        client.getRequestContext().put("security.issue.after.failed.renew", Boolean.TRUE);
        client.getRequestContext().put("security.signature.properties", credential.getProperties());

    }


    @SuppressWarnings("unchecked")
    private void addInterceptors() {
        Bus bus = CXFBusFactory.getDefaultBus();
        bus.getInInterceptors().add(new CookiesInInterceptor());
        bus.getOutInterceptors().add(new CookiesOutInterceptor());
        bus.getOutInterceptors().add(new HeaderInterceptor());
        bus.getInFaultInterceptors().add(new BadTokenInFaultInterceptor());
        bus.getOutInterceptors().add(getWss4JOutInterceptor());
        bus.getOutInterceptors().add(getWss4JOutInterceptor());
    }

    private ClientCallBackHandler getClientCallBackHandler() {
        return new ClientCallBackHandler(dpoUserProperties.getPassword());
    }

    private WSS4JOutInterceptor getWss4JOutInterceptor() {
        Map<String, Object> properties = new HashMap<>();
        properties.put(WSHandlerConstants.ACTION, WSHandlerConstants.USERNAME_TOKEN);
        properties.put(WSHandlerConstants.PASSWORD_TYPE, WSConstants.PW_TEXT);
        properties.put(WSHandlerConstants.USER, dpoUserProperties.getPassword());
        properties.put(WSHandlerConstants.PW_CALLBACK_REF, getClientCallBackHandler());

        return new WSS4JOutInterceptor(properties);
    }
}
