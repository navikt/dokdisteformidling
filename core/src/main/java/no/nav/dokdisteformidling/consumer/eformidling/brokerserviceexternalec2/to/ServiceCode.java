package no.nav.dokdisteformidling.consumer.eformidling.brokerserviceexternalec2.to;


import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ServiceCode {

    private String externalServiceCode;
    private int externalServiceEdictionCode;
}
