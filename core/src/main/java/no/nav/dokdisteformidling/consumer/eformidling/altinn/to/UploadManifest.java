package no.nav.dokdisteformidling.consumer.eformidling.altinn.to;

import lombok.*;

import java.util.List;
import java.util.Map;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UploadManifest {

    private String avgiver;
    private String serviceCode;
    private String serviceEditionCode;
    private List<String> files;
    private String fileZipName;
    private String senderReference; //konversasjonId
    private Map<String,String> properties;

}
