package no.nav.dokdisteformidling.consumer.eformidling.altinn.from;

import lombok.Builder;
import lombok.Value;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.trygderetten.json.KvitteringStatus;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.trygderetten.json.TrygderettenMelding;

@Value
@Builder
public class DownloadResponse {

	String processIdentifier;
	String documentType;
	String conversationId;
	String fileReference;
	String sendersReference;
	String sendtDate;
	KvitteringStatus kvitteringStatus;

	public static DownloadResponse from(AltinnDokument altinnDokument) {
		TrygderettenMelding trygderettenMelding = altinnDokument.getTrygderettenMelding();
		return DownloadResponse.builder()
				.processIdentifier(trygderettenMelding.getProcess())
				.documentType(trygderettenMelding.getStandardBusinessDocumentHeader().getDocumentType())
				.conversationId(trygderettenMelding.getConversationId())
				.fileReference(altinnDokument.getFileReference())
				.sendersReference(altinnDokument.getManifest().getSendersReference())
				.sendtDate(altinnDokument.getManifest().getSentDate().toString())
				.kvitteringStatus(trygderettenMelding.getStatus())
				.build();
	}
}
