package no.nav.dokdisteformidling.consumer.eformidling.altinn.to;

import lombok.*;

import java.util.List;
import java.util.Map;

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
