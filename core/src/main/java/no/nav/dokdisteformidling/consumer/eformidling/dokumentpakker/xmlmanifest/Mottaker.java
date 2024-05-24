package no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.xmlmanifest;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import lombok.Getter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "mottaker")
@XmlRootElement(name = "mottaker")
@Getter
public class Mottaker {

	@XmlElement
	private Organisasjon organisasjon;

	public Mottaker(Organisasjon organisasjon) {
		this.organisasjon = organisasjon;
	}

	public Mottaker() {
		throw new UnsupportedOperationException("Unexpected invocation: Mottaker object is not intended to be used for unmarshalling, only marshalling");
	}

}