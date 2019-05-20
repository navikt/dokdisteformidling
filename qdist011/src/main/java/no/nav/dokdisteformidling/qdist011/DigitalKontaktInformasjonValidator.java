package no.nav.dokdisteformidling.qdist011;

import static no.nav.dokdisteformidling.constants.BridgeMotSDPMapperConstants.DATE_VALID_MONTHS;
import static no.nav.dokdisteformidling.constants.BridgeMotSDPMapperConstants.RESERVASJON;

import no.nav.dokdisteformidling.consumer.dki.HentSikkerDigitalPostadresseResponseTo;
import no.nav.dokdisteformidling.consumer.dokkat.tkat021.VarselInfoTo;
import no.nav.dokdisteformidling.exception.functional.IllegalKontaktInformasjonFunctionalException;
import org.apache.camel.Handler;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.xml.datatype.XMLGregorianCalendar;
import java.util.GregorianCalendar;

/**
 * @author Heidi Elisabeth Sando, Visma Consulting
 */

@Component
public class DigitalKontaktInformasjonValidator {

	@Handler
	public HentSikkerDigitalPostadresseResponseTo validateKontaktinfo(HentSikkerDigitalPostadresseResponseTo hentSikkerDigitalPostadresseResponseTo,
																	  VarselInfoTo varselInfoTo) throws IllegalKontaktInformasjonFunctionalException {
		hentSikkerDigitalPostadresseResponseTo = validateHentSikkerDigitalPostadresseResponseTo(hentSikkerDigitalPostadresseResponseTo,
				varselInfoTo);

		return hentSikkerDigitalPostadresseResponseTo;
	}

	public HentSikkerDigitalPostadresseResponseTo validateHentSikkerDigitalPostadresseResponseTo(
			HentSikkerDigitalPostadresseResponseTo hentSikkerDigitalPostadresseResponseTo,
			VarselInfoTo varselInfoTo) throws IllegalKontaktInformasjonFunctionalException {
		if (hentSikkerDigitalPostadresseResponseTo.getDigitalKontaktinformasjon().getReservasjon().equals(RESERVASJON)) {
			throw new IllegalKontaktInformasjonFunctionalException("Reservert kontaktinformasjon");
		}

		if (!verifyAddress(hentSikkerDigitalPostadresseResponseTo)) {
			throw new IllegalKontaktInformasjonFunctionalException("Manglende sertifikat, leverandoerAdresse eller brukerAdresse");
		}

		if (!varselInfoTo.equals(null)) {
			hentSikkerDigitalPostadresseResponseTo = verifyEmailAndPhone(hentSikkerDigitalPostadresseResponseTo);
		}

		return hentSikkerDigitalPostadresseResponseTo;
	}

	private boolean verifyAddress(HentSikkerDigitalPostadresseResponseTo hentSikkerDigitalPostadresseResponseTo) {
		boolean hasSertifikat = hentSikkerDigitalPostadresseResponseTo.getSertifikat().length == 0;
		boolean hasLeverandorAdresse = StringUtils.isNotBlank(hentSikkerDigitalPostadresseResponseTo.getSikkerDigitalPostkasse()
				.getLeverandoerAdresse());
		boolean hasBrukerAdresse = StringUtils.isNotBlank(hentSikkerDigitalPostadresseResponseTo.getSikkerDigitalPostkasse()
				.getBrukerAdresse());

		return (hasSertifikat && hasLeverandorAdresse && hasBrukerAdresse);
	}

	private boolean isInvalid(XMLGregorianCalendar dateTime) {

		GregorianCalendar calendar = dateTime.toGregorianCalendar();
		GregorianCalendar today = Qdist011FunctionalUtils.getNow().toGregorianCalendar();
		calendar.add(today.MONTH, -DATE_VALID_MONTHS);
		int result = calendar.compareTo(today);        //If result is positive: calendar is later than (today - DATE_VALID_MONTHS):

		return dateTime == null || (result < 0);
	}

	public boolean isEpostDateInvalid(HentSikkerDigitalPostadresseResponseTo.Epostadresse epostadresse) {

		return isInvalid(epostadresse.getSistVerifisert()) && isInvalid(epostadresse.getSistOppdatert());
	}

	public boolean isMobilDateInvalid(HentSikkerDigitalPostadresseResponseTo.Mobiltelefonnummer mobiltelefonnummer) {
		return isInvalid(mobiltelefonnummer.getSistVerifisert()) && isInvalid(mobiltelefonnummer.getSistOppdatert());
	}

	private HentSikkerDigitalPostadresseResponseTo verifyEmailAndPhone(HentSikkerDigitalPostadresseResponseTo hentSikkerDigitalPostadresseResponseTo) throws IllegalKontaktInformasjonFunctionalException {
		HentSikkerDigitalPostadresseResponseTo.Epostadresse epost = hentSikkerDigitalPostadresseResponseTo.getDigitalKontaktinformasjon()
				.getEpostadresse();
		HentSikkerDigitalPostadresseResponseTo.Mobiltelefonnummer mobil = hentSikkerDigitalPostadresseResponseTo.getDigitalKontaktinformasjon()
				.getMobiltelefonnummer();

		if (StringUtils.isBlank(epost.getValue()) && StringUtils.isBlank(mobil.getValue())) {
			throw new IllegalKontaktInformasjonFunctionalException("Epostadresse and mobiltelefonnummer is empty");
		}

		if (isEpostDateInvalid(epost)) {
			hentSikkerDigitalPostadresseResponseTo.getDigitalKontaktinformasjon().setEpostadresse(null);
		}

		if (isMobilDateInvalid(mobil)) {
			hentSikkerDigitalPostadresseResponseTo.getDigitalKontaktinformasjon().setMobiltelefonnummer(null);
		}

		if (isEpostDateInvalid(epost) && isMobilDateInvalid(mobil)) {
			throw new IllegalKontaktInformasjonFunctionalException("Epostadresse and mobiltelefonnummer is invalid");
		}

		return hentSikkerDigitalPostadresseResponseTo;
	}

}
