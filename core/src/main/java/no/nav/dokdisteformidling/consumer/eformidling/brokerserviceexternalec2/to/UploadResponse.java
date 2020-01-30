package no.nav.dokdisteformidling.consumer.eformidling.brokerserviceexternalec2.to;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import no.altinn.brokerserviceexternalbasicstreamed.ReceiptExternalStreamedBE;

@Getter
@Setter
@Builder
public class UploadResponse {

    private String fileReference;
    private ReceiptExternalStreamedBE receiptExternalStreamedBE;
}
