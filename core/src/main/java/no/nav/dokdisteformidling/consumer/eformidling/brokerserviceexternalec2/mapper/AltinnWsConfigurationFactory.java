package no.nav.dokdisteformidling.consumer.eformidling.brokerserviceexternalec2.mapper;

import lombok.RequiredArgsConstructor;
import no.nav.dokdisteformidling.config.props.BrokerServiceExternalEC2Properties;
import no.nav.dokdisteformidling.config.props.BrokerServiceExternalECStreamedProperties;
import no.nav.dokdisteformidling.config.props.DpoUserProperties;
import no.nav.dokdisteformidling.consumer.eformidling.brokerserviceexternalec2.to.AltinnWsConfiguration;
import no.nav.dokdisteformidling.consumer.eformidling.serviceregistry.EformidlingMottakerInfoService;
import no.nav.dokdisteformidling.exception.functional.AltinnWsConfigurationException;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URL;

@Component
@RequiredArgsConstructor
public class AltinnWsConfigurationFactory {

    private final BrokerServiceExternalEC2Properties brokerServiceProperties;
    private final BrokerServiceExternalECStreamedProperties streamedProperties;
    private final DpoUserProperties dpoUserProperties;
    private final EformidlingMottakerInfoService mottakerInfoService;


    public AltinnWsConfiguration create() {
        return AltinnWsConfiguration.builder()
                .brokerServiceUrl(createUrl(brokerServiceProperties.getEndpointurl()))
                .streamingServiceUrl(createUrl(streamedProperties.getEndpointurl()))
                .username(dpoUserProperties.getUsername())
                .password(dpoUserProperties.getPassword())
                .externalServiceCode(mottakerInfoService.hentMottakerInfoTrygderetten().getServiceCode())
                .externalServiceEditionCode(Integer.valueOf(mottakerInfoService.hentMottakerInfoTrygderetten().getServiceEditionCode()))
                .build();
    }


    private URL createUrl(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new AltinnWsConfigurationException("Konfigurert  URL er ugyldig ", e);
        }
    }
}
