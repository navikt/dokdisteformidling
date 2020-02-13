package no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdisteformidling.common.JsonUtils;
import no.nav.dokdisteformidling.common.XmlUtils;
import no.nav.dokdisteformidling.consumer.eformidling.altinn.from.AltinnDokument;
import no.nav.dokdisteformidling.consumer.eformidling.altinn.from.DownloadedMessageFromAltinn;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.exceptions.DokumentpakkingException;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.trygderetten.json.TrygderettenMelding;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.trygderetten.xml.BrokerServiceManifest;
import org.springframework.stereotype.Component;

import javax.xml.bind.JAXBException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
        log.info("Pakker ut fil med referansenummer: " + melding.getFileReference().getFileReference());
        ZipInputStream zipInputStream = new ZipInputStream(melding.getInputStream());

        BrokerServiceManifest manifest;
        TrygderettenMelding trygderettenMelding;
        try (InputStream inputStreamProxy = new FilterInputStream(zipInputStream) {
            @Override
            public void close() {
                // do nothing to avoid unmarshaller to close it before the Zip file is fully processed
            }
        }) {

            ZipEntry zipEntry;
            manifest = null;
            trygderettenMelding = null;

            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                switch (zipEntry.getName()) {
                    case AltinnDokument.MANIFEST_XML_FILENAME:
                        manifest = XmlUtils.unmarshalXmlObject(inputStreamProxy, BrokerServiceManifest.class);
                        break;
                    case AltinnDokument.STANDARDBUSINESSDOCUMENT_JSON_FILENAME:
                        trygderettenMelding = JsonUtils.toObject(inputStreamProxy, TrygderettenMelding.class);
                        break;
                    default:
                        log.info("Hopper over fil: {}", zipEntry.getName());
                }
            }
        }
        return AltinnDokument.builder()
                .manifest(manifest)
                .trygderettenMelding(trygderettenMelding)
                .build();
    }

}
