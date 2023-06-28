package no.nav.dokdisteformidling.consumer.eformidling.altinn.to;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor
public class UploadManifest {

    String avsender;
    String serviceCode;
    String serviceEditionCode;
    String fileZipName;
    String senderReference;
}
