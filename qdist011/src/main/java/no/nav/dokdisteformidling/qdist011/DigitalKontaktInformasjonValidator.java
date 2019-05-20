package no.nav.dokdisteformidling.qdist011;

import static no.nav.dokdisteformidling.constants.BridgeMotSDPMapperConstants.DATE_VALID_MONTHS;
import static no.nav.dokdisteformidling.constants.BridgeMotSDPMapperConstants.RESERVASJON;

import no.nav.dokdisteformidling.consumer.dki.HentSikkerDigitalPostadresseResponseTo;
import no.nav.dokdisteformidling.exception.functional.IllegalKontaktInformasjonException;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.lang3.StringUtils;

import javax.xml.datatype.XMLGregorianCalendar;
import java.util.GregorianCalendar;

/**
 * @author Heidi Elisabeth Sando, Visma Consulting
 */
public class DigitalKontaktInformasjonValidator implements Processor {

	@Override
	public void process(Exchange exchange) throws IllegalKontaktInformasjonException{
		HentSikkerDigitalPostadresseResponseTo hentSikkerDigitalPostadresseResponseTo = exchange.getIn().getBody(HentSikkerDigitalPostadresseResponseTo.class);

		hentSikkerDigitalPostadresseResponseTo = validateHentSikkerDigitalPostadresseResponseTo(hentSikkerDigitalPostadresseResponseTo);

		exchange.getIn().setBody(hentSikkerDigitalPostadresseResponseTo);
	}

	public HentSikkerDigitalPostadresseResponseTo validateHentSikkerDigitalPostadresseResponseTo(HentSikkerDigitalPostadresseResponseTo hentSikkerDigitalPostadresseResponseTo) throws IllegalKontaktInformasjonException {
		if (hentSikkerDigitalPostadresseResponseTo.getDigitalKontaktinformasjon().getReservasjon().equals(RESERVASJON)) {
			throw new IllegalKontaktInformasjonException("Reservert kontaktinformasjon");
		}

		if (!verifyAddress(hentSikkerDigitalPostadresseResponseTo)) {
			throw new IllegalKontaktInformasjonException("Manglende sertifikat, leverandoerAdresse eller brukerAdresse");
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

	private HentSikkerDigitalPostadresseResponseTo verifyEmailAndPhone(HentSikkerDigitalPostadresseResponseTo hentSikkerDigitalPostadresseResponseTo) throws IllegalKontaktInformasjonException {
		HentSikkerDigitalPostadresseResponseTo.Epostadresse epost = hentSikkerDigitalPostadresseResponseTo.getDigitalKontaktinformasjon()
				.getEpostadresse();
		HentSikkerDigitalPostadresseResponseTo.Mobiltelefonnummer mobil = hentSikkerDigitalPostadresseResponseTo.getDigitalKontaktinformasjon()
				.getMobiltelefonnummer();

		if (StringUtils.isBlank(epost.getValue()) && StringUtils.isBlank(mobil.getValue())) {
			throw new IllegalKontaktInformasjonException("Epostadresse and mobiltelefonnummer is empty");
		}

		if (isEpostDateInvalid(epost)) {
			hentSikkerDigitalPostadresseResponseTo.getDigitalKontaktinformasjon().setEpostadresse(null);
		}

		if (isMobilDateInvalid(mobil)) {
			hentSikkerDigitalPostadresseResponseTo.getDigitalKontaktinformasjon().setMobiltelefonnummer(null);
		}

		if (isEpostDateInvalid(epost) && isMobilDateInvalid(mobil)) {
			throw new IllegalKontaktInformasjonException("Epostadresse and mobiltelefonnummer is invalid");
		}

		return hentSikkerDigitalPostadresseResponseTo;
	}

}
