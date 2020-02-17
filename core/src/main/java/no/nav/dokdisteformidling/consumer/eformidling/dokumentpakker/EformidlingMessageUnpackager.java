package no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokdisteformidling.common.AutoCloseableTempFile;
import no.nav.dokdisteformidling.consumer.eformidling.altinn.from.AltinnDokument;
import no.nav.dokdisteformidling.consumer.eformidling.altinn.from.DownloadedMessageFromAltinn;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.exceptions.DokumentUnpackingException;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.trygderetten.json.TrygderettenMelding;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.trygderetten.xml.BrokerServiceManifest;
import no.nav.dokdisteformidling.utils.XmlUtils;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static no.nav.dokdisteformidling.consumer.eformidling.altinn.from.AltinnDokument.MANIFEST_XML;
import static no.nav.dokdisteformidling.consumer.eformidling.altinn.from.AltinnDokument.SBD_JSON;

/**
 * Pakker ut en eformidling melding fra Altinn.
 *
 * Meldingen består av:
 * Konvolutt ((StandardBusinessDocument)sbd.json, manifest.xml)
 */
@Slf4j
@Component
public class EformidlingMessageUnpackager {

    private static final String TEMPFILE_EXCEPTION = "Feil ved innlesing/kopiering av inputStream til temporær fil";
    private static final String UNMARSHALLING_EXCEPTION = "Feil ved unmarshalling av fil med filreferanse: ";

    private final ObjectMapper objectMapper;

    @Inject
    public EformidlingMessageUnpackager(@Named("eformidlingObjectMapper") ObjectMapper eformidlingObjectMapper) {
        this.objectMapper = eformidlingObjectMapper;
    }

    public List<AltinnDokument> unpackageMessages(List<DownloadedMessageFromAltinn> messageFromAltinns) {
        return messageFromAltinns.stream().map(this::unpack).collect(Collectors.toList());
    }

    private AltinnDokument unpack(DownloadedMessageFromAltinn melding) {
        String fileReference = melding.getFileReference().getFileReference();
        log.info("Pakker ut zipfil med referansenummer: {}", fileReference);

        // Trenger tempFile for å lagre inputStream fra Altinn som fil. Får feilmelding hvis vi unmarshaller direkte fra Altinn meldingens inputStream.
        try (AutoCloseableTempFile tempFile = new AutoCloseableTempFile("altinn", "test")) {
            FileUtils.copyInputStreamToFile(melding.getInputStream(), tempFile.toFile());

            return buildAltinnDokumentFromTempFile(tempFile.toFile(), fileReference);
        } catch (IOException e) {
            log.error(TEMPFILE_EXCEPTION, e);
            throw new DokumentUnpackingException(TEMPFILE_EXCEPTION, e);
        }
    }

    private AltinnDokument buildAltinnDokumentFromTempFile(File tempFile, String fileReference) {
        BrokerServiceManifest manifest = null;
        TrygderettenMelding trygderettenMelding = null;

        try (ZipFile zipFile = new ZipFile(tempFile)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();  // entries = manifest.xml || sbd.json
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                final InputStream inputStream = zipFile.getInputStream(zipEntry);
                if (MANIFEST_XML.equals(zipEntry.getName())) {
                    manifest = XmlUtils.unmarshalXmlObject(inputStream, BrokerServiceManifest.class);
                } else if (SBD_JSON.equals(zipEntry.getName())) {
                    trygderettenMelding = objectMapper.readValue(inputStream, TrygderettenMelding.class);
                } else {
                    log.info("Hopper over fil: {}", zipFile.getName());
                }
            }
        } catch (JAXBException | IOException e) {
            log.error(UNMARSHALLING_EXCEPTION + fileReference, e);
            throw new DokumentUnpackingException(UNMARSHALLING_EXCEPTION + fileReference, e);
        }
        return AltinnDokument.builder().fileReference(fileReference).manifest(manifest).trygderettenMelding(trygderettenMelding).build();

    }

}
