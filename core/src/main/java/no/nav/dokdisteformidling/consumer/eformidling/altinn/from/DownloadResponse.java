package no.nav.dokdisteformidling.consumer.eformidling.altinn.from;

import lombok.Builder;
import lombok.Value;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.trygderetten.json.KvitteringStatus;

@Value
@Builder
public class DownloadResponse {
    private final String conversationId;
    private final String fileReference;
    private final String sendersReference;
    private final String sendtDate;
    private final KvitteringStatus kvitteringStatus;
}
