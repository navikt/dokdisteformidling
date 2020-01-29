package no.nav.dokdisteformidling.config.cxf;


import no.nav.dokdisteformidling.config.interceptor.*;
import no.nav.dokdisteformidling.config.props.DpoUserProperties;
import org.apache.cxf.Bus;
import org.apache.cxf.bus.CXFBusFactory;
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;
import org.apache.wss4j.dom.WSConstants;
import org.apache.wss4j.dom.handler.WSHandlerConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class BrokerServiceExternalEC2Config {


    @Bean
    public Bus addInterceptors(DpoUserProperties dpoUserProperties) {
        Bus bus = CXFBusFactory.getDefaultBus();
        bus.getInInterceptors().add(new CookiesInInterceptor());
        bus.getOutInterceptors().add(new CookiesOutInterceptor());
        bus.getOutInterceptors().add(new HeaderInterceptor());
        bus.getInFaultInterceptors().add(new BadTokenInFaultInterceptor());
        bus.getOutInterceptors().add(getWss4JOutInterceptor(dpoUserProperties));
        bus.getOutInterceptors().add(getWss4JOutInterceptor(dpoUserProperties));
        return bus;
    }

    private ClientCallBackHandler getClientCallBackHandler(DpoUserProperties dpoUserProperties) {
        return new ClientCallBackHandler(dpoUserProperties.getPassword());
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
