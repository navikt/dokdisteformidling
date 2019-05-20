package no.nav.dokdisteformidling.consumer.dki;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Value;
import no.nav.dokdisteformidling.exception.functional.IllegalKontaktInformasjonException;
import no.nav.dokdisteformidling.exception.technical.KunneIkkeHenteDagensDatoTechnicalException;
import org.apache.commons.lang3.StringUtils;


import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.GregorianCalendar;

/**
 * @author Erik Bråten, Visma Consulting.
 */
@Value
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
		private Epostadresse epostadresse;
		private Mobiltelefonnummer mobiltelefonnummer;
	}

	@Value
	@Builder
	public static class Epostadresse {
		private final String value;
		private final XMLGregorianCalendar sistVerifisert;
		private final XMLGregorianCalendar sistOppdatert;
	}

	@Value
	@Builder
	public static class Mobiltelefonnummer {
		private final String value;
		private final XMLGregorianCalendar sistOppdatert;
		private final XMLGregorianCalendar sistVerifisert;
	}

	@Value
	@Builder
	public static class DigitalPostkasse {
		private final String leverandoerAdresse;
		private final String brukerAdresse;
	}

}
