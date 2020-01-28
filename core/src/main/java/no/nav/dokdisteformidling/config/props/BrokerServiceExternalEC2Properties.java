package no.nav.dokdisteformidling.config.props;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;


@Getter
@Setter
@ConfigurationProperties("altinn.brokerserviceexternalec2.url")
@Validated
public class BrokerServiceExternalEC2Properties {

    @NotEmpty
    private String endpointurl;
    @Min(1)
    private int readtimeoutms;
    @Min(1)
    private int connecttimeoutms;
}
