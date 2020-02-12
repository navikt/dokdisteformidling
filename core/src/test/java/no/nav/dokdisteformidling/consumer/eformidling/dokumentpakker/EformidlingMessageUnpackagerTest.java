package no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker;

import no.altinn.schema.services.serviceengine.broker._2015._06.BrokerServiceManifest;
import no.nav.dokdisteformidling.consumer.eformidling.altinn.from.AltinnDokument;
import no.nav.dokdisteformidling.consumer.eformidling.altinn.from.DownloadedMessageFromAltinn;
import no.nav.dokdisteformidling.consumer.eformidling.altinn.to.FileReference;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.sbdh.StandardBusinessDocument;
import org.junit.jupiter.api.Test;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.Assertions.assertThat;

public class EformidlingMessageUnpackagerTest {

    private final static String SENDERS_REFERENCE = "33259df3-18ae-45e6-9861-47f42e119a14";
    private final static String CONVERSATION_ID = "6205a3bb-e12a-4913-99db-877339e14496";

    private final EformidlingMessageUnpackager eformidlingMessageUnpackager = new EformidlingMessageUnpackager();
    private int receiptId = 0;

    private final static Long MILLIS_IN_A_DAY = 86400000L;

    @Test
    void shouldUnpackTestZipFile() {

        String fileName = "src/test/resources/zip/zipEgen.zip";
        List<DownloadedMessageFromAltinn> messageFromAltinns = new ArrayList<>();
        AltinnDokument altinnDokument = null;

        try (FileInputStream fileInputStream = new FileInputStream(fileName)) {
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);

            ZipInputStream zipInputStream = new ZipInputStream(bufferedInputStream);
            ZipEntry zipEntry;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                System.out.format("File: %s Size: %d Last Modified %s %n",
                        zipEntry.getName(), zipEntry.getSize(),
                        LocalDate.ofEpochDay(zipEntry.getTime() / MILLIS_IN_A_DAY));
            }

            messageFromAltinns.add(DownloadedMessageFromAltinn.builder()
                    .fileReference(createFileReference("fileReference"))
                    .inputStream(bufferedInputStream)
                    .build());
            altinnDokument = eformidlingMessageUnpackager.unpackageMessages(messageFromAltinns).get(0);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        BrokerServiceManifest actualManifest = altinnDokument.getManifest();
        assertThat(actualManifest).isNotNull();
        assertThat(actualManifest.getSendersReference()).isEqualTo(SENDERS_REFERENCE);

        StandardBusinessDocument actualSbd = altinnDokument.getSbd();
        assertThat(actualSbd).isNotNull();
        assertThat(actualSbd.getConversationId()).isEqualTo(CONVERSATION_ID);

        Set<KvitteringStatusMessage> kvitteringStatusMessages = ((ArkivmeldingKvitteringMessage) actualSbd.getForretningsmelding()).getMessage();
        assertThat(kvitteringStatusMessages.size()).isEqualTo(1);

    }

    private FileReference createFileReference(String filreference) {
        receiptId += 1;
        return new FileReference(filreference, receiptId);
    }

}
