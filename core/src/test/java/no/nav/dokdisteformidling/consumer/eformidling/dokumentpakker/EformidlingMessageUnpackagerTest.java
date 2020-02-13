package no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker;

import no.nav.dokdisteformidling.consumer.eformidling.altinn.from.AltinnDokument;
import no.nav.dokdisteformidling.consumer.eformidling.altinn.from.DownloadedMessageFromAltinn;
import no.nav.dokdisteformidling.consumer.eformidling.altinn.to.FileReference;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.trygderetten.json.KvitteringStatus;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.trygderetten.json.TrygderettenMelding;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.trygderetten.xml.BrokerServiceManifest;
import org.junit.jupiter.api.Test;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class EformidlingMessageUnpackagerTest {

    private final static String SENDERS_REFERENCE = "33259df3-18ae-45e6-9861-47f42e119a14";
    private final static String CONVERSATION_ID = "f1b3002b-1dea-4c14-8072-8c191183d04c";

    private final EformidlingMessageUnpackager eformidlingMessageUnpackager = new EformidlingMessageUnpackager();

    @Test
    void shouldUnpackTestZipFile() throws Exception {

        String fileName = "src/test/resources/zip/altinn_sbd_kvittering.zip";
        List<DownloadedMessageFromAltinn> messageFromAltinns = new ArrayList<>();
        AltinnDokument altinnDokument;

        try (FileInputStream fileInputStream = new FileInputStream(fileName)) {
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);

            messageFromAltinns.add(DownloadedMessageFromAltinn.builder()
                    .fileReference(new FileReference("fileReference", 1))
                    .inputStream(bufferedInputStream)
                    .build());
            altinnDokument = eformidlingMessageUnpackager.unpackageMessages(messageFromAltinns).get(0);
        }

        BrokerServiceManifest actualManifest = altinnDokument.getManifest();
        assertThat(actualManifest).isNotNull();
        assertThat(actualManifest.getSendersReference()).isEqualTo(SENDERS_REFERENCE);

        TrygderettenMelding actualSbd = altinnDokument.getTrygderettenMelding();
        assertThat(actualSbd).isNotNull();
        assertThat(actualSbd.getConversationId()).isEqualTo(CONVERSATION_ID);

        KvitteringStatus status = actualSbd.getStatus();
        assertThat(status).isNotNull();
        assertThat(status.getStatus()).isEqualTo("MOTTATT");

    }
}
