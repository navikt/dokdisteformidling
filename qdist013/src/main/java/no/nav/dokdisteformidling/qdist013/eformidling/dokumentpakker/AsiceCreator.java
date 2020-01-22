package no.nav.dokdisteformidling.qdist013.eformidling.dokumentpakker;

import lombok.extern.slf4j.Slf4j;
import no.difi.asic.AsicWriter;
import no.difi.asic.AsicWriterFactory;
import no.difi.asic.MimeType;
import no.difi.asic.SignatureHelper;
import no.nav.dokdisteformidling.certificate.AppCertificate;
import no.nav.dokdisteformidling.qdist013.eformidling.NavDokument;
import org.springframework.stereotype.Component;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Slf4j
class AsiceCreator {
	public OutputStream createAsiceStreamed(Stream<? extends NavDokument> dokumenter,
											AppCertificate appCertificate) throws IOException {
		ByteArrayOutputStream asiceArchive = new ByteArrayOutputStream();
		AsicWriter asicWriter = AsicWriterFactory.newFactory()
				.newContainer(asiceArchive);
		List<InputStream> streamsToClose = new ArrayList<>();

		try {
			dokumenter.forEach(f -> {
				try {
					if(log.isDebugEnabled()) {
						log.debug("Adding file {} of type {}", f.getFilnavn(), f.getMimeType());
					}
					InputStream inputStream = new BufferedInputStream(f.getContents());
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
