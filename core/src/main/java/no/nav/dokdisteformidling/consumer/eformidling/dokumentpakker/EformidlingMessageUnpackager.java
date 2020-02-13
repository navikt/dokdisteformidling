package no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdisteformidling.common.JsonUtils;
import no.nav.dokdisteformidling.common.XmlUtils;
import no.nav.dokdisteformidling.consumer.eformidling.altinn.from.AltinnDokument;
import no.nav.dokdisteformidling.consumer.eformidling.altinn.from.DownloadedMessageFromAltinn;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.exceptions.DokumentpakkingException;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.trygderetten.json.TrygderettenMelding;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.trygderetten.xml.BrokerServiceManifest;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Pakker ut en eformidling melding fra Altinn.
 *
 * Meldingen består av:
 * Konvolutt ((StandardBusinessDocument)sbd.json, manifest.xml)
 */
@Slf4j
@Component
public class EformidlingMessageUnpackager {

    private static final String IO_EXCEPTION = "Feil med IO ved unmarshalling";
    private static final String UNMARSHALLING_EXCEPTION = "Feil ved unmarshalling med JAXB";


    public List<AltinnDokument> unpackageMessages(List<DownloadedMessageFromAltinn> messageFromAltinns) {
        List<AltinnDokument> altinnDokuments = new ArrayList<>();
        messageFromAltinns.forEach(downloadedMessageFromAltinn -> {
            try {
                altinnDokuments.add(unpack(downloadedMessageFromAltinn));
            } catch (JAXBException e) {
                log.error(UNMARSHALLING_EXCEPTION, e);
                throw new DokumentpakkingException(UNMARSHALLING_EXCEPTION, e);
            } catch (IOException e) {
                log.error(IO_EXCEPTION, e);
                throw new DokumentpakkingException(IO_EXCEPTION, e);
            }
        });

        return altinnDokuments;
    }

    private AltinnDokument unpack(DownloadedMessageFromAltinn melding) throws JAXBException, IOException {
        String filReference = melding.getFileReference().getFileReference();
        log.info("Pakker ut zipfil med referansenummer: " + filReference);

        final Path tempFile = Files.createTempFile("altinn", "test");
        FileUtils.copyInputStreamToFile(this.getClass().getResourceAsStream("/zip/altinn_sbd_kvittering.zip"), tempFile.toFile());
        BrokerServiceManifest manifest = null;
        TrygderettenMelding trygderettenMelding = null;

        try (ZipFile zipFile = new ZipFile(tempFile.toFile())) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                log.info(filReference + " inneholder: " + zipEntry.getName());
                final InputStream inputStream = zipFile.getInputStream(zipEntry);
                if (AltinnDokument.MANIFEST_XML_FILENAME.equals(zipEntry.getName())) {
                    manifest = XmlUtils.unmarshalXmlObject(inputStream, BrokerServiceManifest.class);
                } else if (AltinnDokument.STANDARDBUSINESSDOCUMENT_JSON_FILENAME.equals(zipEntry.getName())) {
                    trygderettenMelding = JsonUtils.toObject(inputStream, TrygderettenMelding.class);
                } else {
                    log.info("Hopper over fil " + zipEntry.getName());
                }
            }
        }
        return AltinnDokument.builder()
                .manifest(manifest)
                .trygderettenMelding(trygderettenMelding)
                .build();
    }
}
