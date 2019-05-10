package no.nav.dokdisteformidling.consumer.dki;

import lombok.Builder;
import lombok.Data;

import javax.xml.datatype.XMLGregorianCalendar;

/**
 * @author Erik Bråten, Visma Consulting.
 */
@Data
@Builder
public class HentSikkerDigitalPostadresseResponseTo {

	private final Kontaktinformasjon digitalKontaktinformasjon;
	private final DigitalPostkasse sikkerDigitalPostkasse;
	private final byte[] sertifikat;

	@Data
	@Builder
	public static class Kontaktinformasjon {
		private final String personident;
		private final String reservasjon;
		private final Epostadresse epostadresse;
		private final Mobiltelefonnummer mobiltelefonnummer;
	}

	@Data
	@Builder
	public static class Epostadresse {
		private final String value;
		private final XMLGregorianCalendar sistVerifisert;
		private final XMLGregorianCalendar sistOppdatert;
	}

	@Data
	@Builder
	public static class Mobiltelefonnummer {
		private final String value;
		private final XMLGregorianCalendar sistOppdatert;
		private final XMLGregorianCalendar sistVerifisert;
	}

	@Data
	@Builder
	public static class DigitalPostkasse {
		private final String leverandoerAdresse;
		private final String brukerAdresse;
	}
}
