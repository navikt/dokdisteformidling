package no.nav.dokdisteformidling.consumer.eformidling.brokerserviceexternalec2.to;

import lombok.Builder;
import lombok.Getter;
import lombok.Value;

import java.net.URL;


@Builder
@Getter
@Value
public class AltinnWsConfiguration {

    private URL streamingServiceUrl;
    private URL brokerServiceUrl;
    private String username;
    private String password;
    private String externalServiceCode;
    private int externalServiceEditionCode;

}
