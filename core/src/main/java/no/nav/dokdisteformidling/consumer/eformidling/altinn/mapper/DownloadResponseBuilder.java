package no.nav.dokdisteformidling.consumer.eformidling.altinn.mapper;

import no.nav.dokdisteformidling.consumer.eformidling.altinn.from.AltinnDokument;
import no.nav.dokdisteformidling.consumer.eformidling.altinn.from.DownloadResponse;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.trygderetten.json.KvitteringStatus;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.trygderetten.json.TrygderettenMelding;

public class DownloadResponseBuilder {

    String conversationId;
    String fileReference;
    String sendersReference;
    String sendtDate;
    KvitteringStatus kvitteringStatus;

    public DownloadResponseBuilder withAltinnDokument(AltinnDokument altinnDokument) {

        TrygderettenMelding trygderettenMelding = altinnDokument.getTrygderettenMelding();
        this.conversationId = trygderettenMelding.getConversationId();
        this.fileReference = altinnDokument.getFileReference();
        this.sendersReference = altinnDokument.getManifest().getSendersReference();
        this.sendtDate = altinnDokument.getManifest().getSentDate().toString();
        this.kvitteringStatus = trygderettenMelding.getStatus();
        return this;
    }

    public DownloadResponse build() {
        return DownloadResponse.builder()
                .conversationId(conversationId)
                .fileReference(fileReference)
                .sendersReference(sendersReference)
                .sendtDate(sendtDate)
                .kvitteringStatus(kvitteringStatus)
                .build();
    }
}
