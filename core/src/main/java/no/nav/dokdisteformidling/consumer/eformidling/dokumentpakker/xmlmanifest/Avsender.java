package no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.xmlmanifest;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "avsender")
@XmlRootElement(name = "avsender")
public class Avsender {
  @XmlElement
	private Organisasjon organisasjon;

	public Avsender(Organisasjon organisasjon) {
		super();
		this.organisasjon = organisasjon;
	}

	public Avsender() {
		super();
	}
}
