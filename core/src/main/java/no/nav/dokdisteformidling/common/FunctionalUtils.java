package no.nav.dokdisteformidling.common;

import static java.lang.String.format;

import com.amazonaws.SdkClientException;
import no.nav.dokdisteformidling.constants.DomainConstants;
import no.nav.dokdisteformidling.consumer.rdist001.HentForsendelseResponseTo;
import no.nav.dokdisteformidling.exception.functional.InvalidForsendelseStatusFunctionalException;
import no.nav.dokdisteformidling.exception.functional.KunneIkkeDeserialisereS3JsonPayloadFunctionalException;
import no.nav.dokdisteformidling.exception.technical.KunneIkkeHenteDagensDatoTechnicalException;
import no.nav.dokdisteformidling.exception.technical.KunneIkkeKonvertereTilXmlGregorianCalendarTechnicalException;
import no.nav.dokdisteformidling.storage.DokdistDokument;
import no.nav.dokdisteformidling.storage.JsonSerializer;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.GregorianCalendar;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Heidi Elisabeth Sando, Visma Consulting.
 */

public class FunctionalUtils {

	private FunctionalUtils() {
	}

	public static void validateThatForsendelseStatusIsKlarForDist(String forsendelseStatus) {
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

	public static DokdistDokument deserializeS3JsonPayloadToDokdistDokument(String jsonPayload, String objektReferanse) {
		DokdistDokument dokdistDokument;
		try {
			dokdistDokument = JsonSerializer.deserialize(jsonPayload, DokdistDokument.class);
			dokdistDokument.setDokumentObjektReferanse(objektReferanse);
		} catch (SdkClientException e) {
			throw new KunneIkkeDeserialisereS3JsonPayloadFunctionalException(format("Kunne ikke deserialisere jsonPayload fra s3 bucket for dokument med dokumentobjektreferanse=%s. Dokumentet er ikke persistert til s3 med korrekt format!", objektReferanse));
		}
		return dokdistDokument;
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

	public static String generateRandomUUID() {
		return UUID.randomUUID().toString();
	}

}
