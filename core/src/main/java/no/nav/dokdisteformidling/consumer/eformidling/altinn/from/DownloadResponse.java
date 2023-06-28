package no.nav.dokdisteformidling.consumer.eformidling.altinn.from;

import lombok.Builder;
import lombok.Value;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.trygderetten.json.KvitteringStatus;

@Value
@Builder
public class DownloadResponse {

    String conversationId;
    String fileReference;
    String sendersReference;
    String sendtDate;
    KvitteringStatus kvitteringStatus;
}
