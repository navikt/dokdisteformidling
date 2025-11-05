package no.nav.dokdisteformidling.sdist001.itest.config;

import no.altinn.brokerserviceexternal.IBrokerServiceExternal;
import no.nav.dokdisteformidling.config.cxf.AbstractCxfEndpointConfig;
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
@Profile("itest")
public class BrokerServiceExternalTestConfig extends AbstractCxfEndpointConfig {

    public BrokerServiceExternalTestConfig(Bus bus, DpoUserProperties dpoUserProperties) {
        super(bus, dpoUserProperties);
    }

    @Bean
    public IBrokerServiceExternal iBrokerServiceExternal(BrokerServiceExternalProperties brokerServiceExternalProperties, DpoUserProperties dpoUserProperties) {
        setWsdlUrl("wsdl/BrokerServiceExternalTest.wsdl");
        setServiceName(SERVICE);
        setEndpointName(CustomBindingIBrokerServiceExternal);
        setAddress(brokerServiceExternalProperties.getEndpointurl());
        setReceiveTimeout(brokerServiceExternalProperties.getReadtimeoutms());
        setConnectTimeout(brokerServiceExternalProperties.getConnecttimeoutms());

        addInInterceptor(new CookiesInInterceptor());
        addOutInterceptor(new HeaderOutInterceptor());
        addOutInterceptor(new CookiesOutInterceptor());

        IBrokerServiceExternal iBrokerServiceExternal = createPort(IBrokerServiceExternal.class);
        final Client client = ClientProxy.getClient(iBrokerServiceExternal);
        setRequestContext(client);

        return iBrokerServiceExternal;
    }
}
