package no.nav.dokdisteformidling.consumer.eformidling.altinn.to;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import no.altinn.brokerserviceexternalstreamed.ReceiptExternalStreamedBE;

@Getter
@Setter
@Builder
public class UploadResponse {

    private String fileReference;
    private ReceiptExternalStreamedBE receiptExternalStreamedBE;

    public UploadResponse(String fileReference, ReceiptExternalStreamedBE receiptExternalStreamedBE) {
        this.fileReference = fileReference;
        this.receiptExternalStreamedBE = receiptExternalStreamedBE;
    }
}
