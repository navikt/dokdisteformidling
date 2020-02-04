package no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokdisteformidling.certificate.AppCertificate;
import no.nav.dokdisteformidling.consumer.eformidling.AltinnPackage;
import no.nav.dokdisteformidling.consumer.eformidling.NavDokumentpakke;
import no.nav.dokdisteformidling.consumer.eformidling.altinn.to.DownloadedFileFromAltinn;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.exceptions.DokumentpakkingException;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.sbdh.StandardBusinessDocument;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.*;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static no.nav.dokdisteformidling.consumer.eformidling.EformidlingConstants.EFORMIDLING_ASIC;
import static no.nav.dokdisteformidling.consumer.eformidling.EformidlingConstants.EFORMIDLING_SBD;

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
		final StandardBusinessDocument envelope = standardBusinessDocumentMapper.mapArkivmeldingEnvelope(navDokumentpakke.getConversationId(),
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
			if (konvolutt.getAny() instanceof ArkivmeldingMessage) {
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

	public TrygderettDokumentpakke unpackMessage(List<AltinnPackage> trygderettDokumenter) {
		trygderettDokumenter.stream().map(altinnPackage -> altinnPackage.)

		downloadedFileFromAltinns.stream().map(this::createSbd).collect(Collectors.toList());

	}

	private StandardBusinessDocument createSbd(DownloadedFileFromAltinn fileFromAltinn) throws IOException {
		ByteArrayOutputStream buffOS = new ByteArrayOutputStream();
		fileFromAltinn.getDataHandler().writeTo(buffOS);

		File outFile = File.createTempFile(fileFromAltinn.getFileReference().getFileReference(), ".zip");
		try (FileOutputStream fos = new FileOutputStream(outFile)) {
			fos.write(buffOS.toByteArray());
		}

		log.info("Skrev til fil " + outFile.getAbsolutePath());
	}
}
