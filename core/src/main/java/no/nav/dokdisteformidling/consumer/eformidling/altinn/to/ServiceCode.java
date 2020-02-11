package no.nav.dokdisteformidling.consumer.eformidling.altinn.to;


import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ServiceCode {

    private String serviceCode;
    private int serviceEditionCode;
}
