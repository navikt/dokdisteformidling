package no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.xmlmanifest;

import no.nav.dokdisteformidling.consumer.eformidling.NavDokument;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.xmlmanifest.Avsender;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.xmlmanifest.HovedDokument;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.xmlmanifest.Manifest;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.xmlmanifest.MarshalManifest;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.xmlmanifest.Mottaker;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.xmlmanifest.Organisasjon;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;


public class XmlManifestCreator {
	private static final String HOVEDDOKUMENT = "Hoveddokument";
	private static final String HOVEDDOKUMENT_LANG = "no";

	public String createManifest(final NavDokument arkivmelding, final String senderIdentifier, final String receiverIdentifier) {
		Avsender avsender = new Avsender(new Organisasjon(senderIdentifier));
		Mottaker mottaker = new Mottaker(new Organisasjon(receiverIdentifier));
		HovedDokument hoveddokumentXml = new HovedDokument(arkivmelding.getFilnavn(), arkivmelding.getMimeType(), HOVEDDOKUMENT, HOVEDDOKUMENT_LANG);
		Manifest xmlManifest = new Manifest(mottaker, avsender, hoveddokumentXml);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		MarshalManifest.marshal(xmlManifest, os);
		return new String(os.toByteArray(), StandardCharsets.UTF_8);
	}
}
