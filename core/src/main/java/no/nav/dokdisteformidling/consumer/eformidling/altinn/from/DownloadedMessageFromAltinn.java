package no.nav.dokdisteformidling.consumer.eformidling.altinn.from;

import lombok.Builder;
import lombok.Value;

import java.io.InputStream;

@Value
@Builder
public class DownloadedMessageFromAltinn {
    private final String filreferanse;
    private final InputStream inputStream;
}
