package no.nav.dokdisteformidling;

import static java.nio.charset.StandardCharsets.UTF_8;
import static no.nav.dokdisteformidling.qdist011.Qdist011FunctionalUtils.getNow;
import static no.nav.dokdisteformidling.qdist011.constants.BridgeMotSDPMapperConstants.DATE_VALID_MONTHS;
import static no.nav.dokdisteformidling.qdist011.constants.BridgeMotSDPMapperConstants.EPOST;
import static no.nav.dokdisteformidling.qdist011.constants.BridgeMotSDPMapperConstants.SMS;
import static org.apache.xml.security.stax.ext.XMLSecurityConstants.datatypeFactory;

import no.nav.dokdisteformidling.exception.technical.KunneIkkeHenteDagensDatoTechnicalException;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
public final class testUtils {
	private testUtils() {
	}

	public static String classpathToString(String classpathResource) throws IOException {
		InputStream inputStream = new ClassPathResource(classpathResource).getInputStream();
		String message = IOUtils.toString(inputStream, UTF_8);
		IOUtils.closeQuietly(inputStream);
		return message;
	}

	public static String fileToString(File file) throws IOException {
		byte[] data = new byte[(int) file.length()];
		try (FileInputStream fis = new FileInputStream(file)) {
			fis.read(data);
		}
		return new String(data);
	}

	public static XMLGregorianCalendar makeUgyldigDate() {
		XMLGregorianCalendar calendar = getNow();
		GregorianCalendar gregorianCalendar = calendar.toGregorianCalendar();
		gregorianCalendar.add(Calendar.MONTH, - (DATE_VALID_MONTHS + 1));

		return datatypeFactory.newXMLGregorianCalendar(gregorianCalendar);
	}

	public static XMLGregorianCalendar getDateOnly(XMLGregorianCalendar dateTime) {

		DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date();

		try {
			dateTime = DatatypeFactory.newInstance().newXMLGregorianCalendar(format.format(date));
		}catch(DatatypeConfigurationException e){
			throw new KunneIkkeHenteDagensDatoTechnicalException("Kunne ikke gjøre om dagens dato og tid til kun dato", e);
		}
		return dateTime;
	}

	public static Set<String> makePreferertKanalSet(String... preferertKanal) {
		Set<String> set = new HashSet<String>();

		for (String kanal : preferertKanal) {
			set.add(kanal);
		}
		return set;
	}

	public static java.util.Map<String, String> varslingsTekster(String epostVarslingsTekst, String smsVarslingsTekst) {
		Map<String, String> varslingsMap = new HashMap<String, String>();
		varslingsMap.put(EPOST, epostVarslingsTekst);
		varslingsMap.put(SMS, smsVarslingsTekst);
		return varslingsMap;
	}
}
