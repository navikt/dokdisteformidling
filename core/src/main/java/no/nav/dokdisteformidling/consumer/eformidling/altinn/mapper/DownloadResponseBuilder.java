package no.nav.dokdisteformidling.consumer.eformidling.altinn.mapper;

import no.nav.dokdisteformidling.consumer.eformidling.altinn.from.AltinnDokument;
import no.nav.dokdisteformidling.consumer.eformidling.altinn.from.DownloadResponse;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.ArkivmeldingKvitteringMessage;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.KvitteringStatusMessage;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.sbdh.StandardBusinessDocument;

import java.util.Set;

public class DownloadResponseBuilder {

    String conversationId;
    String sendersReference;
    String sendtDate;
    Set<KvitteringStatusMessage> kvitteringStatus;

    public DownloadResponseBuilder withAltinnDokument(AltinnDokument altinnDokument) {

        StandardBusinessDocument sbd = altinnDokument.getSbd();
        this.conversationId = sbd.getConversationId();
        this.sendersReference = altinnDokument.getManifest().getSendersReference();
        this.sendtDate = altinnDokument.getManifest().getSentDate().toString();
        this.kvitteringStatus = ((ArkivmeldingKvitteringMessage) sbd.getForretningsmelding()).getMessage();
        return this;
    }

    public DownloadResponse build() {
        return DownloadResponse.builder()
                .conversationId(conversationId)
                .sendersReference(sendersReference)
                .sendtDate(sendtDate)
                .kvitteringStatus(kvitteringStatus)
                .build();
    }
}
