package no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.xmlmanifest;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.XmlValue;

import static no.nav.dokdisteformidling.consumer.eformidling.Organisasjonsnummer.ISO6523_AUTHORITY;
import static no.nav.dokdisteformidling.consumer.eformidling.Organisasjonsnummer.ISO6523_PREFIX;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "organisasjon")
@XmlRootElement(name = "organiasjon")
public class Organisasjon {
	@XmlAttribute
	private String authority;
	@XmlValue
	private String orgNummer;

	public Organisasjon(final String orgNummer) {
		super();
		this.authority = ISO6523_AUTHORITY;
		this.orgNummer = ISO6523_PREFIX + orgNummer;
	}
}
