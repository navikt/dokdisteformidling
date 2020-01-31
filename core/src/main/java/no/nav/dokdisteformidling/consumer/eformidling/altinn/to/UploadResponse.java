package no.nav.dokdisteformidling.consumer.eformidling.altinn.to;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;
import no.altinn.brokerserviceexternalstreamed.ReceiptExternalStreamedBE;

@Value
@Builder
public class UploadResponse {
    private final String fileReference;
    private final ReceiptTo receiptTo;
}
