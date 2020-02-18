package no.nav.dokdisteformidling.utils;

import no.nav.dokdisteformidling.exception.technical.KunneIkkeHenteDagensDatoTechnicalException;
import no.nav.dokdisteformidling.exception.technical.KunneIkkeKonvertereTilXmlGregorianCalendarTechnicalException;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.GregorianCalendar;

import static java.lang.String.format;

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
        try {
            return DatatypeFactory.newInstance()
                    .newXMLGregorianCalendar(localDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        } catch (DatatypeConfigurationException e) {
            throw new KunneIkkeKonvertereTilXmlGregorianCalendarTechnicalException(format("Kunne ikke konvertere fra localDateTime til XmlGregorianCalendar. Forsøkte å konvertere localDateTime=%s", localDateTime == null ? null : localDateTime
                    .toString()), e);
        }
    }
}
