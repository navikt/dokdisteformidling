package no.nav.dokdisteformidling.qdist011;

import static no.nav.dokdisteformidling.qdist011.constants.BridgeMotSDPMapperConstants.DATE_VALID_MONTHS;
import static no.nav.dokdisteformidling.qdist011.constants.BridgeMotSDPMapperConstants.RESERVASJON;

import no.nav.dokdisteformidling.consumer.dki.HentSikkerDigitalPostadresseResponseTo;
import no.nav.dokdisteformidling.consumer.dokkat.tkat021.VarselInfoTo;
import no.nav.dokdisteformidling.exception.functional.IllegalKontaktInformasjonFunctionalException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.xml.datatype.XMLGregorianCalendar;
import java.util.GregorianCalendar;

/**
 * @author Heidi Elisabeth Sando, Visma Consulting
 */

@Component
public class DigitalKontaktInformasjonValidator {

	public HentSikkerDigitalPostadresseResponseTo validateKontaktinfo(HentSikkerDigitalPostadresseResponseTo hentSikkerDigitalPostadresseResponseTo,
																	  VarselInfoTo varselInfoTo) {
		validateHentSikkerDigitalPostadresseResponseTo(hentSikkerDigitalPostadresseResponseTo, varselInfoTo);

		return hentSikkerDigitalPostadresseResponseTo;
	}

	public void validateHentSikkerDigitalPostadresseResponseTo(HentSikkerDigitalPostadresseResponseTo hentSikkerDigitalPostadresseResponseTo,
															   VarselInfoTo varselInfoTo) {
		if (hentSikkerDigitalPostadresseResponseTo.getDigitalKontaktinformasjon().getReservasjon().equals(RESERVASJON)) {
			throw new IllegalKontaktInformasjonFunctionalException("Reservert kontaktinformasjon");
		}

		if (!hasValidSertifikatAndAdresses(hentSikkerDigitalPostadresseResponseTo)) {
			throw new IllegalKontaktInformasjonFunctionalException("Manglende sertifikat, leverandoerAdresse eller brukerAdresse");
		}

		if (!(varselInfoTo == null)) {
			verifyEmailAndPhone(hentSikkerDigitalPostadresseResponseTo);
		}

	}

	private boolean hasValidSertifikatAndAdresses(HentSikkerDigitalPostadresseResponseTo hentSikkerDigitalPostadresseResponseTo) {

		boolean hasSertifikat = (hentSikkerDigitalPostadresseResponseTo.getSertifikat() != null) &&
				(hentSikkerDigitalPostadresseResponseTo.getSertifikat().length > 0);


		boolean hasLeverandorAdresse = (hentSikkerDigitalPostadresseResponseTo.getDigitalKontaktinformasjon() != null) &&
				StringUtils.isNotBlank(hentSikkerDigitalPostadresseResponseTo.getSikkerDigitalPostkasse()
						.getLeverandoerAdresse());

		boolean hasBrukerAdresse = (hentSikkerDigitalPostadresseResponseTo.getSikkerDigitalPostkasse() != null) &&
				StringUtils.isNotBlank(hentSikkerDigitalPostadresseResponseTo.getSikkerDigitalPostkasse().getBrukerAdresse());

		return (hasSertifikat && hasLeverandorAdresse && hasBrukerAdresse);
	}

	private static boolean isInvalidDate(XMLGregorianCalendar dateTime) {

		int result = -1;

		if (!(dateTime == null)) {
			GregorianCalendar calendar = dateTime.toGregorianCalendar();
			GregorianCalendar outdated = Qdist011FunctionalUtils.getNow().toGregorianCalendar();
			outdated.add(GregorianCalendar.MONTH, -DATE_VALID_MONTHS);
			result = calendar.compareTo(outdated);        //If result is positive: calendar is later than (today - DATE_VALID_MONTHS):
		}

		return result < 0;
	}

	public static boolean isEpostDateInvalid(HentSikkerDigitalPostadresseResponseTo.Epostadresse epostadresse) {
		return isInvalidDate(epostadresse.getSistVerifisert()) && isInvalidDate(epostadresse.getSistOppdatert());
	}

	public static boolean isMobilDateInvalid(HentSikkerDigitalPostadresseResponseTo.Mobiltelefonnummer mobiltelefonnummer) {
		return isInvalidDate(mobiltelefonnummer.getSistVerifisert()) && isInvalidDate(mobiltelefonnummer.getSistOppdatert());
	}

	private void verifyEmailAndPhone(HentSikkerDigitalPostadresseResponseTo hentSikkerDigitalPostadresseResponseTo) {
		HentSikkerDigitalPostadresseResponseTo.Epostadresse epost = hentSikkerDigitalPostadresseResponseTo.getDigitalKontaktinformasjon()
				.getEpostadresse();
		HentSikkerDigitalPostadresseResponseTo.Mobiltelefonnummer mobil = hentSikkerDigitalPostadresseResponseTo.getDigitalKontaktinformasjon()
				.getMobiltelefonnummer();

		if (StringUtils.isBlank(epost.getValue()) && StringUtils.isBlank(mobil.getValue())) {
			throw new IllegalKontaktInformasjonFunctionalException("Epostadresse og mobiltelefonnummer er tom");
		}

		if (isEpostDateInvalid(epost) && isMobilDateInvalid(mobil)) {
			throw new IllegalKontaktInformasjonFunctionalException("Epostadresse og mobiltelefonnummer er invalid");
		}
	}
}
