package no.nav.dokdisteformidling.consumer.eformidling.altinn.to;

import lombok.Builder;
import lombok.Value;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.sbdh.StandardBusinessDocument;

import java.util.List;

@Value
@Builder
public class DownloadResponse {
    private final List<StandardBusinessDocument> standardBusinessDocuments;
}
