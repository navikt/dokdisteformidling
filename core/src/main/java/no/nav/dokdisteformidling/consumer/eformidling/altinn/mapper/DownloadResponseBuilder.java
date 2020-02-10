package no.nav.dokdisteformidling.consumer.eformidling.altinn.mapper;

import no.nav.dokdisteformidling.consumer.eformidling.altinn.from.DownloadResponse;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.ArkivmeldingKvitteringMessage;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.KvitteringStatusMessage;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.sbdh.StandardBusinessDocument;

import java.util.Set;

public class DownloadResponseBuilder {

    String conversationId;
    Set<KvitteringStatusMessage> kvitteringStatus;

    public DownloadResponseBuilder withStandardBusinessDocument(StandardBusinessDocument sdb) {
        this.conversationId = sdb.getConversationId();
        this.kvitteringStatus = ((ArkivmeldingKvitteringMessage) sdb.getForretningsmelding()).getMessage();
        return this;
    }

    public DownloadResponse build() {
        return DownloadResponse.builder()
                .conversationId(conversationId)
                .kvitteringStatus(kvitteringStatus)
                .build();
    }
}
