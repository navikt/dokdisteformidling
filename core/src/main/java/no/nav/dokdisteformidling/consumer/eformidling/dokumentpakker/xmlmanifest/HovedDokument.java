package no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.xmlmanifest;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.XmlValue;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "hoveddokument")
@XmlRootElement(name = "hoveddokument")
public class HovedDokument {

	@XmlAttribute
	private final String href;

	@XmlAttribute
	private final String mime;

	@XmlElement
	private final Tittel tittel;

	public HovedDokument(String href, String mime, String tittel, String lang) {
		this.href = href;
		this.mime = mime;
		this.tittel = new Tittel(tittel, lang);
	}

	public HovedDokument() {
		throw new UnsupportedOperationException("Unexpected invocation: HovedDokument object is not intended to be used for unmarshalling, only marshalling");
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "tittel")
	@XmlRootElement(name = "tittel")
	public static class Tittel {

		@XmlValue
		private final String tittel;

		@XmlAttribute
		private final String lang;

		public Tittel(String tittel, String lang) {
			this.tittel = tittel;
			this.lang = lang;
		}

		public Tittel() {
			throw new UnsupportedOperationException("Unexpected invocation: Tittel object is not intended to be used for unmarshalling, only marshalling");
		}
	}

}