package no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker;

import lombok.extern.slf4j.Slf4j;
import no.difi.asic.AsicWriter;
import no.difi.asic.AsicWriterFactory;
import no.difi.asic.MimeType;
import no.difi.asic.SignatureHelper;
import no.nav.dokdisteformidling.certificate.AppCertificate;
import no.nav.dokdisteformidling.consumer.eformidling.NavDokument;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.exceptions.DokumentpakkingException;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.xmlmanifest.XmlManifestCreator;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static no.nav.dokdisteformidling.consumer.eformidling.EformidlingConstants.NAV_ORGNUMMER;
import static no.nav.dokdisteformidling.consumer.eformidling.EformidlingConstants.TRYGDERETTEN_ORGNUMMER;

/**
 * Endret og tilpasset for NAV sin bruk fra https://github.com/difi/move-integrasjonspunkt
 * <p>
 * Lager asic og signerer denne med virksomhetssertifikat.
 *
 * @author Joakim Bjørnstad, Jbit AS
 */
@Slf4j
class AsiceCreator {
    private final XmlManifestCreator xmlManifestCreator;

    public AsiceCreator() {
        xmlManifestCreator = new XmlManifestCreator();
    }

    OutputStream createAsiceStreamed(NavDokument arkivmelding,
                                     Stream<? extends NavDokument> dokumenter,
                                     AppCertificate appCertificate) throws IOException {
        ByteArrayOutputStream asiceArchive = new ByteArrayOutputStream();
        String xmlManifest = xmlManifestCreator.createManifest(arkivmelding, NAV_ORGNUMMER, TRYGDERETTEN_ORGNUMMER);
        AsicWriter asicWriter = AsicWriterFactory.newFactory()
                .newContainer(asiceArchive)
                .add(new BufferedInputStream(new ByteArrayInputStream(xmlManifest.getBytes())), "manifest.xml", MimeType.XML);
        List<InputStream> streamsToClose = new ArrayList<>();
        try {
            // Skriv arkivmelding til Asice
            InputStream arkivmeldingInputStream = new BufferedInputStream(arkivmelding.getInnhold());
            streamsToClose.add(arkivmeldingInputStream);
            asicWriter.add(arkivmeldingInputStream, arkivmelding.getFilnavn(), MimeType.forString(arkivmelding.getMimeType()));
            // Skriv resten av dokumentene til Asice
            dokumenter.forEach(f -> {
                try {
                    if (log.isDebugEnabled()) {
                        log.debug("Adding file {} of type {}", f.getFilnavn(), f.getMimeType());
                    }
                    InputStream inputStream = new BufferedInputStream(f.getInnhold());
                    streamsToClose.add(inputStream);
                    asicWriter.add(inputStream, f.getFilnavn(), MimeType.forString(f.getMimeType()));
                } catch (IOException e) {
                    throw new DokumentpakkingException("Kunne ikke pakke asice", e);
                }
            });
            asicWriter.sign(new DefaultSignatureHelper(appCertificate));
            return asiceArchive;
        } finally {
            for (InputStream is : streamsToClose) {
                is.close();
            }
        }
    }

    private static class DefaultSignatureHelper extends SignatureHelper {
        DefaultSignatureHelper(AppCertificate appCertificate) {
            super(appCertificate.shouldLockProvider() ? appCertificate.getKeyStore().getProvider() : null);
            loadCertificate(appCertificate.getKeyStore(), appCertificate.getProperties().getAlias(), appCertificate.getProperties().getPassword());
        }
    }
}
