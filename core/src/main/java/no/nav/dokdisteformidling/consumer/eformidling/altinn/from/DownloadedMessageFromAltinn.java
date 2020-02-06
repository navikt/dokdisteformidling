package no.nav.dokdisteformidling.consumer.eformidling.altinn.from;

import lombok.Builder;
import lombok.Value;
import no.nav.dokdisteformidling.consumer.eformidling.altinn.to.FileReference;

import javax.activation.DataHandler;

@Value
@Builder
public class DownloadedMessageFromAltinn {
    private final FileReference fileReference;
    private final DataHandler dataHandler;
}
