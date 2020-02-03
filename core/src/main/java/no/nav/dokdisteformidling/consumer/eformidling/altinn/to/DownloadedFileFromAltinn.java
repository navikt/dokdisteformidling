package no.nav.dokdisteformidling.consumer.eformidling.altinn.to;

import lombok.Builder;
import lombok.Value;

import javax.activation.DataHandler;

@Value
@Builder
public class DownloadedFileFromAltinn {
	private final FileReference fileReference;
	private final DataHandler dataHandler;
}
