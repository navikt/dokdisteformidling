package no.nav.dokdisteformidling.consumer.eformidling.altinn.to;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UploadResponse {
    private final String fileReference;
    private final ReceiptTo receiptTo;
}
