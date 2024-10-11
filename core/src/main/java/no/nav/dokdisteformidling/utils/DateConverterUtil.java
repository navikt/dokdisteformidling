package no.nav.dokdisteformidling.utils;

import no.nav.dokdisteformidling.exception.technical.KunneIkkeHenteDagensDatoTechnicalException;
import no.nav.dokdisteformidling.exception.technical.KunneIkkeKonvertereTilXmlGregorianCalendarTechnicalException;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.time.LocalDateTime;
import java.util.GregorianCalendar;

import static java.lang.String.format;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;

public class DateConverterUtil {

	private DateConverterUtil() {
	}

	public static XMLGregorianCalendar getNow() {
		XMLGregorianCalendar now;
		try {
			now = DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar());
		} catch (DatatypeConfigurationException e) {
			throw new KunneIkkeHenteDagensDatoTechnicalException("Kunne ikke hente dagens dato", e);
		}
		return now;
	}

	public static XMLGregorianCalendar convertLocalDateTimeToXmlGregorianCalendar(LocalDateTime localDateTime) {
		if (localDateTime == null) {
			throw new KunneIkkeKonvertereTilXmlGregorianCalendarTechnicalException("Kunne ikke konvertere fra localDateTime til XmlGregorianCalendar. localDateTime=null");
		}
		try {
			return DatatypeFactory.newInstance()
					.newXMLGregorianCalendar(localDateTime.format(ISO_LOCAL_DATE_TIME));
		} catch (DatatypeConfigurationException e) {
			throw new KunneIkkeKonvertereTilXmlGregorianCalendarTechnicalException(
					format("Kunne ikke konvertere fra localDateTime til XmlGregorianCalendar. Forsøkte å konvertere localDateTime=%s", localDateTime), e);
		}
	}
}
