package no.nav.dokdisteformidling.consumer.eformidling.altinn.to;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor
public class UploadManifest {
    private String avsender;
    private String serviceCode;
    private String serviceEditionCode;
    private String fileZipName;
    private String senderReference;
}
