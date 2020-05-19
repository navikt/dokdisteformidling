package no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokdisteformidling.certificate.AppCertificate;
import no.nav.dokdisteformidling.consumer.eformidling.NavDokumentpakke;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.exceptions.DokumentpakkingException;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.sbdh.StandardBusinessDocument;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.cert.X509Certificate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Pakker NAV dokumentpakke til eformidling melding.
 *
 * Melding består av:
 * Konvolutt (StandardBusinessDocumentHeader, Forretningsmelding)
 * Innhold (Kryptert ASIC-E)
 *
 * https://difi.github.io/felleslosninger/eformidling_nm_message.html
 *
 * @author Joakim Bjørnstad, Jbit AS
 */
@Slf4j
@Component
public class EformidlingMessagePackager {

    public static final String EFORMIDLING_SBD = "sbd.json";
    public static final String EFORMIDLING_ASIC = "asic.zip";

    private final ObjectMapper objectMapper;
    private final StandardBusinessDocumentMapper standardBusinessDocumentMapper;
    private final EformidlingContentPackager eformidlingContentPackager;

    @Inject
    public EformidlingMessagePackager(@Named("eformidlingObjectMapper") ObjectMapper eformidlingObjectMapper,
                                      StandardBusinessDocumentMapper standardBusinessDocumentMapper,
                                      EformidlingContentPackager eformidlingContentPackager) {
        this.objectMapper = eformidlingObjectMapper;
        this.standardBusinessDocumentMapper = standardBusinessDocumentMapper;
        this.eformidlingContentPackager = eformidlingContentPackager;
    }

    public InputStream packageMessage(NavDokumentpakke navDokumentpakke,
                                      AppCertificate appCertificate,
                                      X509Certificate mottakerCertificate) {
        final StandardBusinessDocument envelope = standardBusinessDocumentMapper.mapAvtaltmeldingEnvelope(navDokumentpakke.getConversationId(),
                navDokumentpakke.getBestillingsId());
        final InputStream content = eformidlingContentPackager.packageContent(navDokumentpakke, appCertificate, mottakerCertificate);
        final ByteArrayOutputStream zipfile = new ByteArrayOutputStream();
        writeZip(envelope, content, zipfile);
        final byte[] zip = zipfile.toByteArray();
        log.info("Laget eformidling dokumentpakke zip. filstørrelse={}, conversationId={}, bestillingsId={}", zip.length,
                navDokumentpakke.getConversationId(), navDokumentpakke.getBestillingsId());
        return new ByteArrayInputStream(zip);
    }

    private void writeZip(StandardBusinessDocument konvolutt, InputStream innhold, OutputStream outputStream) {
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {
            if (konvolutt.getAny() instanceof AvtaltMessage) {
                zipOutputStream.putNextEntry(new ZipEntry(EFORMIDLING_SBD));
                objectMapper.writeValue(zipOutputStream, konvolutt);
                zipOutputStream.closeEntry();
            }
            zipOutputStream.putNextEntry(new ZipEntry(EFORMIDLING_ASIC));
            IOUtils.copy(innhold, zipOutputStream);
            zipOutputStream.closeEntry();
            zipOutputStream.finish();
        } catch (IOException e) {
            throw new DokumentpakkingException("Klarte ikke lage sbd.zip", e);
        }
    }
}
