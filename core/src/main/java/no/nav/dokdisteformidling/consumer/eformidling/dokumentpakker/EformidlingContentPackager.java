package no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker;

import no.nav.dokdisteformidling.certificate.AppCertificate;
import no.nav.dokdisteformidling.consumer.eformidling.NavDokumentpakke;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.exceptions.DokumentpakkingException;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.cert.X509Certificate;

/**
 * Pakker en NAV dokumentpakke til en eformidling dokumentpakke.
 *
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class EformidlingContentPackager {
    private final AsiceCreator asiceCreator;
    private final CmsUtil cmsUtil;

    @Inject
    public EformidlingContentPackager() {
        this.asiceCreator = new AsiceCreator();
        this.cmsUtil = new CmsUtil();
    }

    InputStream packageContent(NavDokumentpakke navDokumentpakke,
                               AppCertificate appCertificate,
                               X509Certificate mottakerCertificate) {
        try (final OutputStream asiceStreamed = asiceCreator.createAsiceStreamed(
                navDokumentpakke.getArkivmelding(),
                navDokumentpakke.getNavDokumenter().stream(),
                appCertificate)) {
            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            cmsUtil.createCMSStreamed(new ByteArrayInputStream(((ByteArrayOutputStream) asiceStreamed).toByteArray()),
                    outputStream, mottakerCertificate);
            return new ByteArrayInputStream(outputStream.toByteArray());
        } catch (IOException e) {
            throw new DokumentpakkingException("Klarte ikke lage asic eller kryptere dokumentpakke.", e);
        }
    }
}
