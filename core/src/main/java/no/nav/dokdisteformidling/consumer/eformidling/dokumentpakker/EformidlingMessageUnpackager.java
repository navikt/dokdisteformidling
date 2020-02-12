package no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdisteformidling.consumer.eformidling.altinn.from.AltinnDokument;
import no.nav.dokdisteformidling.consumer.eformidling.altinn.from.DownloadedMessageFromAltinn;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.exceptions.DokumentpakkingException;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.sbdh.BrokerServiceManifest;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.sbdh.StandardBusinessDocument;
import org.springframework.stereotype.Component;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Pakker ut en eformidling melding fra Altinn.
 *
 * Meldingen består av:
 * Konvolutt (StandardBusinessDocumentHeader, ArkivmeldingKvittering)
 */
@Slf4j
@Component
public class EformidlingMessageUnpackager {

    private static final String IO_EXCEPTION = "Feil med IO ved unmarshalling";
    private static final String JAXB_CONTEXTCREATION_EXCEPTION = "Kan ikke opprette JAXBContext: ";
    private static final String UNMARSHALLING_EXCEPTION = "Feil ved unmarshalling med JAXB";
    private static JAXBContext jaxbContext;

    static {
        try {
            jaxbContext = JAXBContext.newInstance(new Class[]{BrokerServiceManifest.class, StandardBusinessDocument.class}, new HashMap());
        } catch (JAXBException e) {
            log.error(JAXB_CONTEXTCREATION_EXCEPTION, e);
            throw new DokumentpakkingException(JAXB_CONTEXTCREATION_EXCEPTION, e);
        }
    }

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
        StandardBusinessDocument sbd;
        BrokerServiceManifest manifest;

        try (InputStream inputStreamProxy = new FilterInputStream(zipInputStream) {
            @Override
            public void close() {
                // do nothing to avoid unmarshaller to close it before the Zip file is fully processed
            }
        }) {
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

            ZipEntry zipEntry;
            manifest = null;
            sbd = null;

            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                switch (zipEntry.getName()) {
                    case AltinnDokument.MANIFEST_XML_FILENAME:
                        manifest = (BrokerServiceManifest) unmarshaller.unmarshal(inputStreamProxy);
                        break;
                    case AltinnDokument.STANDARDBUSINESSDOCUMENT_JSON_FILENAME:
                        sbd = (StandardBusinessDocument) unmarshaller.unmarshal(inputStreamProxy);
                        break;
                    default:
                        log.info("Hopper over fil: {}", zipEntry.getName());
                }
            }
        }
        return AltinnDokument.builder()
                .manifest(manifest)
                .sbd(sbd)
                .build();
    }

}
