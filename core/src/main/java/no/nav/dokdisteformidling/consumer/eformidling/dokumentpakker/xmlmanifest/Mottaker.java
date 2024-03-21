package no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.xmlmanifest;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "mottaker")
@XmlRootElement(name = "mottaker")
public class Mottaker {
	@XmlElement
	private Organisasjon organisasjon;

	public Mottaker(Organisasjon organisasjon) {
		super();
		this.organisasjon = organisasjon;
	}

	public Mottaker() {
		super();
	}

}
