package no.nav.dokdisteformidling.consumer.eformidling.brokerserviceexternalec2.to;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import no.altinn.brokerserviceexternaec2.BrokerServiceAvailableFileStatus;

import java.util.Date;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class SearchCriteria {

    private BrokerServiceAvailableFileStatus availableFileStatus;
    private Date minSentDate;
    private Date maxSentDAte;
}
