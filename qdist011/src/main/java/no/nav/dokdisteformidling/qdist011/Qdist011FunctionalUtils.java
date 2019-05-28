package no.nav.dokdisteformidling.qdist011;

import static no.nav.dokdisteformidling.qdist011.constants.BridgeMotSDPMapperConstants.DATE_VALID_MONTHS;
import static no.nav.dokdisteformidling.qdist011.constants.BridgeMotSDPMapperConstants.EPOST;
import static no.nav.dokdisteformidling.qdist011.constants.BridgeMotSDPMapperConstants.SMS;
import static org.apache.xml.security.stax.ext.XMLSecurityConstants.datatypeFactory;

import no.nav.dokdisteformidling.constants.DomainConstants;
import no.nav.dokdisteformidling.consumer.rdist001.HentForsendelseResponseTo;
import no.nav.dokdisteformidling.exception.functional.InvalidForsendelseStatusFunctionalException;
import no.nav.dokdisteformidling.exception.technical.KunneIkkeHenteDagensDatoTechnicalException;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Heidi Elisabeth Sando, Visma Consulting.
 */

public class Qdist011FunctionalUtils {

	private Qdist011FunctionalUtils() {
	}

	public static void validateForsendelseStatus(String forsendelseStatus) {
		if (!DomainConstants.FORSENDELSE_STATUS_KLAR_FOR_DIST.equals(forsendelseStatus)) {
			throw new InvalidForsendelseStatusFunctionalException(String.format("ForsendelseStatus må være %s. Fant forsendelseStatus=%s",
					DomainConstants.FORSENDELSE_STATUS_KLAR_FOR_DIST, forsendelseStatus));
		}
	}

	public static String getDokumenttypeIdHoveddokument(HentForsendelseResponseTo hentForsendelseResponseTo) {
		return hentForsendelseResponseTo.getDokumenter().stream()
				.filter(dokumentTo -> DomainConstants.HOVEDDOKUMENT.equals(dokumentTo.getTilknyttetSom()))
				.map(HentForsendelseResponseTo.DokumentTo::getDokumenttypeId)
				.collect(Collectors.toList())
				.get(0);
	}

	public static XMLGregorianCalendar getNow() {
		XMLGregorianCalendar now;
		try {
			now = DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar());
		} catch (DatatypeConfigurationException e) {
			throw new KunneIkkeHenteDagensDatoTechnicalException("qdist011 kunne ikke hente dagens dato", e);
		}
		return now;
	}

	public static XMLGregorianCalendar getNowDate() {
		XMLGregorianCalendar now;

		String formater = "yyyy-MM-dd";
		DateFormat format = new SimpleDateFormat(formater);
		Date date = new Date();

		try {
			now = DatatypeFactory.newInstance().newXMLGregorianCalendar(format.format(date));
		} catch (DatatypeConfigurationException e) {
			throw new KunneIkkeHenteDagensDatoTechnicalException("qdist011 kunne ikke hente dagens dato", e);
		}
		return now;
	}

	public static XMLGregorianCalendar makeUgyldigDate() {
		XMLGregorianCalendar calendar = getNowDate();
		GregorianCalendar gregorianCalendar = calendar.toGregorianCalendar();
		gregorianCalendar.add(Calendar.MONTH, -(DATE_VALID_MONTHS + 1));

		return datatypeFactory.newXMLGregorianCalendar(gregorianCalendar);
	}

	public static Set<String> makePreferertKanalSet(String... preferertKanal) {
		Set<String> set = new HashSet<String>();

		for (String kanal : preferertKanal) {
			set.add(kanal);
		}
		return set;
	}

	public static java.util.Map<String, String> varslingsTekst(String epostVarslingsTekst, String smsVarslingsTekst) {
		Map<String, String> varslingsMap = new HashMap<String, String>();
		varslingsMap.put(EPOST, epostVarslingsTekst);
		varslingsMap.put(SMS, smsVarslingsTekst);
		return varslingsMap;
	}

}
