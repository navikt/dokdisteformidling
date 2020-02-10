package no.nav.dokdisteformidling.consumer.eformidling.altinn.from;

import lombok.Builder;
import lombok.Value;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.KvitteringStatusMessage;

import java.util.Set;

@Value
@Builder
public class DownloadResponse {
    private final String conversationId;
    private final Set<KvitteringStatusMessage> kvitteringStatus;
}
