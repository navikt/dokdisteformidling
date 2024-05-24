package no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.xmlmanifest;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import lombok.Getter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Manifest", propOrder = { "mottaker", "avsender", "hoveddokument", })
@XmlRootElement(name = "manifest")
@Getter
public class Manifest {

	@XmlElement(required = true)
	private final Mottaker mottaker;

	@XmlElement(required = true)
	private final Avsender avsender;

	@XmlElement(required = true)
	private final HovedDokument hoveddokument;

	public Manifest(Mottaker mottaker, Avsender avsender, HovedDokument hoveddokument) {
		this.mottaker = mottaker;
		this.avsender = avsender;
		this.hoveddokument = hoveddokument;
	}

	public Manifest() {
		throw new UnsupportedOperationException("Unexpected invocation: Manifest object is not intended to be used for unmarshalling, only marshalling");
	}

}
