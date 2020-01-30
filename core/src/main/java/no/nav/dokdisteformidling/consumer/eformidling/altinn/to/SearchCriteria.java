package no.nav.dokdisteformidling.consumer.eformidling.altinn.to;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import no.altinn.brokerserviceexternal.BrokerServiceAvailableFileStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class SearchCriteria {

    private BrokerServiceAvailableFileStatus availableFileStatus;
    private LocalDateTime minSentDate;
    private LocalDateTime maxSentDate;
}
