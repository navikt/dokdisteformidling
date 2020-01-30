package no.nav.dokdisteformidling.consumer.eformidling.brokerserviceexternalec2.to;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ServiceCode {

    private String externalServiceCode;
    private int externalServiceEdictionCode;
}
