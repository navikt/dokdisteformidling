package no.nav.dokdisteformidling.qdist013.eformidling.dokumentpakker;

import no.nav.dokdisteformidling.certificate.AppCertificate;
import no.nav.dokdisteformidling.qdist013.eformidling.NavDokumentpakke;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.cert.X509Certificate;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class EformidlingMessagePackager {
	private final AsiceCreator asiceCreator;
	private final CmsUtil cmsUtil;

	@Inject
	public EformidlingMessagePackager() {
		this.asiceCreator = new AsiceCreator();
		this.cmsUtil = new CmsUtil();
	}

	public InputStream createEformidlingMessage(NavDokumentpakke navDokumentpakke,
												AppCertificate appCertificate,
												X509Certificate mottakerCertificate) {
		// TODO vi burde ha pipes her for å unngå mye minnebruk
		try {
			final OutputStream asiceStreamed = asiceCreator.createAsiceStreamed(navDokumentpakke.getNavDokumenter().stream(), appCertificate);
			final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			cmsUtil.createCMSStreamed(new ByteArrayInputStream(((ByteArrayOutputStream) asiceStreamed).toByteArray()), outputStream, mottakerCertificate);
			return new ByteArrayInputStream(outputStream.toByteArray());
		} catch (IOException e) {
			throw new DokumentpakkingException("Klarte ikke pakke dokumentpakke.", e);
		}
	}
}
