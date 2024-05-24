package no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.xmlmanifest;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.XmlValue;
import lombok.Getter;

import static no.nav.dokdisteformidling.consumer.eformidling.Organisasjonsnummer.ISO6523_AUTHORITY;
import static no.nav.dokdisteformidling.consumer.eformidling.Organisasjonsnummer.ISO6523_PREFIX;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "organisasjon")
@XmlRootElement(name = "organiasjon")
@Getter
public class Organisasjon {

	@XmlAttribute
	private final String authority;

	@XmlValue
	private final String orgNummer;

	public Organisasjon(final String orgNummer) {
		this.authority = ISO6523_AUTHORITY;
		this.orgNummer = ISO6523_PREFIX + orgNummer;
	}

	public Organisasjon() {
		throw new UnsupportedOperationException("Unexpected invocation: Organisasjon object is not intended to be used for unmarshalling, only marshalling");
	}

}
