package no.nav.dokdisteformidling.consumer.eformidling.altinn.to;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class DownloadResponse {
	private final List<DownloadedFileFromAltinn> downloadedFileFromAltinn;
}
