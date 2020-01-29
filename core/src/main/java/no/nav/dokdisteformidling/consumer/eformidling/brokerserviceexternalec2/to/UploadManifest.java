package no.nav.dokdisteformidling.consumer.eformidling.brokerserviceexternalec2.to;

import lombok.*;
import no.nav.dokdisteformidling.consumer.eformidling.serviceregistry.MottakerInfo;

import java.util.List;
import java.util.Map;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UploadManifest {

    private MottakerInfo mottakerInfo;
    private List<String> files;
    private String fileZipName;
    private String senderReference;
    private Map<String,String> properties;

}
