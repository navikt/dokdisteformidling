package no.nav.dokdisteformidling.qdist013;

import javax.xml.datatype.XMLGregorianCalendar;
import java.time.LocalDateTime;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
public final class TestUtil {

	private TestUtil() {
	}

	public static LocalDateTime convertFromXmlGregorianCalendarToLocalDateTime(XMLGregorianCalendar xmlGregorianCalendar) {
		return xmlGregorianCalendar.toGregorianCalendar().toZonedDateTime().toLocalDateTime();
	}
}
