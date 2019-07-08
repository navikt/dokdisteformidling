package no.nav.dokdisteformidling.qdist013;

import static no.nav.dokdisteformidling.common.FunctionalUtils.convertLocalDateTimeToXmlGregorianCalendar;
import static no.nav.dokdisteformidling.common.FunctionalUtils.generateRandomUUID;
import static no.nav.dokdisteformidling.common.FunctionalUtils.getNow;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import no.arkivverket.standarder.noark5.arkivmelding.Arkivmelding;
import no.arkivverket.standarder.noark5.arkivmelding.Dokumentbeskrivelse;
import no.arkivverket.standarder.noark5.arkivmelding.Dokumentobjekt;
import no.arkivverket.standarder.noark5.arkivmelding.Journalpost;
import no.arkivverket.standarder.noark5.arkivmelding.Korrespondansepart;
import no.arkivverket.standarder.noark5.arkivmelding.ObjectFactory;
import no.arkivverket.standarder.noark5.arkivmelding.Saksmappe;
import no.arkivverket.standarder.noark5.arkivmelding.Sakspart;
import no.arkivverket.standarder.noark5.metadatakatalog.Dokumentstatus;
import no.arkivverket.standarder.noark5.metadatakatalog.Journalposttype;
import no.arkivverket.standarder.noark5.metadatakatalog.Journalstatus;
import no.arkivverket.standarder.noark5.metadatakatalog.Saksstatus;
import no.arkivverket.standarder.noark5.metadatakatalog.TilknyttetRegistreringSom;
import no.nav.dokdisteformidling.consumer.rdist001.HentForsendelseResponseTo;
import no.nav.dokdisteformidling.qdist013.saf.JournalpostQdist013;
import org.springframework.stereotype.Component;

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.IntStream;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Component
public class ArkivmeldingMapper {

	public static final String SYSTEM_DOKDISTEFORMIDLING = "dokdisteformidling";
	public static final String NAV_KLAGEINSTANS = "NAV Klageinstans";
	public static final String SAKSPART_ROLLE_DAP = "DAP";
	public static final String SAKSPART_ROLLE_AMP = "AMP";

	public JAXBElement<Arkivmelding> createArkivMelding(HentForsendelseResponseTo hentForsendelseResponseTo, JournalpostQdist013 journalpostQdist013) {
		ObjectFactory objectFactory = new ObjectFactory();
		final String systemId = generateRandomUUID(); //TODO: Korrekt å bruke samme systemId over alt?
		final XMLGregorianCalendar datoArkivmeldingOpprettet = getNow();

		Arkivmelding arkivmelding = objectFactory.createArkivmelding();
		arkivmelding.setSystem(SYSTEM_DOKDISTEFORMIDLING);
		arkivmelding.setMeldingId(hentForsendelseResponseTo.getBestillingsId());
		arkivmelding.setTidspunkt(datoArkivmeldingOpprettet);
		arkivmelding.setAntallFiler(hentForsendelseResponseTo.getDokumenter().size());
		arkivmelding.getMappe()
				.add(createAndPopulateSaksmappe(hentForsendelseResponseTo, journalpostQdist013, systemId, datoArkivmeldingOpprettet, objectFactory));

		return objectFactory.createArkivmelding(arkivmelding);
	}

	private Saksmappe createAndPopulateSaksmappe(HentForsendelseResponseTo hentForsendelseResponseTo,
												 JournalpostQdist013 journalpostQdist013,
												 String systemId,
												 XMLGregorianCalendar datoArkivmeldingOpprettet,
												 ObjectFactory objectFactory) {
		Saksmappe saksmappe = objectFactory.createSaksmappe();
		saksmappe.setSystemID(systemId);
//		saksmappe.setTittel(); Todo Dette skal vøre tema decoded
		saksmappe.setOpprettetDato(convertLocalDateTimeToXmlGregorianCalendar(journalpostQdist013.getSak().getDatoOpprettet()));
		saksmappe.setOpprettetAv(journalpostQdist013.getOpprettetAvNavn());
		saksmappe.getBasisregistrering()
				.add(createAndPopulateJournalpost(journalpostQdist013, systemId, datoArkivmeldingOpprettet, objectFactory));
		saksmappe.setSaksdato(convertLocalDateTimeToXmlGregorianCalendar(journalpostQdist013.getSak().getDatoOpprettet()));
		saksmappe.setAdministrativEnhet(NAV_KLAGEINSTANS);
		saksmappe.setSaksansvarlig(journalpostQdist013.getOpprettetAvNavn());
		saksmappe.setSaksstatus(Saksstatus.UNDER_BEHANDLING);
		saksmappe.getSakspart()
				.add(createAndPopulateSakspartAMP(journalpostQdist013, objectFactory));
		saksmappe.getSakspart()
				.add(createAndPopulateSakspartDAP(journalpostQdist013, objectFactory));
		return saksmappe;
	}

	private Journalpost createAndPopulateJournalpost(JournalpostQdist013 journalpostQdist013, String systemId, XMLGregorianCalendar datoArkivmeldingOpprettet, ObjectFactory objectFactory) {
		Journalpost journalpost = objectFactory.createJournalpost();
		journalpost.setSystemID(systemId);
		journalpost.setOpprettetDato(convertLocalDateTimeToXmlGregorianCalendar(journalpostQdist013.getDatoOpprettet()));
		journalpost.setOpprettetAv(journalpostQdist013.getOpprettetAvNavn());
		journalpost.setReferanseForelderMappe(systemId);
		addDokumentBeskrivelserToJournalpost(journalpost, journalpostQdist013, systemId, datoArkivmeldingOpprettet, objectFactory);
		journalpost.setTittel(journalpostQdist013.getTittel());
		journalpost.setJournalposttype(Journalposttype.UTGÅENDE_DOKUMENT);
		journalpost.setJournalstatus(Journalstatus.EKSPEDERT);
		journalpost.setJournaldato(convertLocalDateTimeToXmlGregorianCalendar(journalpostQdist013.getDatoJournalfoert()));
		journalpost.getKorrespondansepart()
				.add(createAndPolpulateKorrespondanspartMottaker(journalpostQdist013, systemId, objectFactory));
		journalpost.getKorrespondansepart()
				.add(createAndPolpulateKorrespondanspartAvsender(journalpostQdist013, systemId, objectFactory));
		return journalpost;
	}

	private void addDokumentBeskrivelserToJournalpost(Journalpost journalpost,
													  JournalpostQdist013 journalpostQdist013,
													  String systemId,
													  XMLGregorianCalendar datoArkivmeldingOpprettet,
													  ObjectFactory objectFactory) {
		List<Object> dokumentbeskrivelser = journalpost.getDokumentbeskrivelseAndDokumentobjekt();
		IntStream.range(0, journalpostQdist013.getDokumenter().size())
				.forEach(i -> {
					JournalpostQdist013.DokumentInfo dokumentInfo = journalpostQdist013.getDokumenter().get(i);
					dokumentbeskrivelser.add(createAndPopulateDokumentBeskrivelse(journalpostQdist013, dokumentInfo, i, systemId, datoArkivmeldingOpprettet, objectFactory));
				});
	}

	private Dokumentbeskrivelse createAndPopulateDokumentBeskrivelse(JournalpostQdist013 journalpostQdist013,
																	 JournalpostQdist013.DokumentInfo dokumentInfo,
																	 int rekkefolge,
																	 String systemId,
																	 XMLGregorianCalendar datoArkivmeldingOpprettet,
																	 ObjectFactory objectFactory) {
		Dokumentbeskrivelse dokumentbeskrivelse = objectFactory.createDokumentbeskrivelse();
		dokumentbeskrivelse.setSystemID(systemId);
		dokumentbeskrivelse.setDokumenttype(journalpostQdist013.getKategori());
		dokumentbeskrivelse.setDokumentstatus(Dokumentstatus.DOKUMENTET_ER_FERDIGSTILT);
//		dokumentbeskrivelse.setTittel(); TODO: Må avklares
		dokumentbeskrivelse.setOpprettetDato(convertLocalDateTimeToXmlGregorianCalendar(dokumentInfo.getDatoFerdigstilt()));
		dokumentbeskrivelse.setOpprettetAv(getDokumentOpprettetAv(isHoveddokument(rekkefolge), journalpostQdist013, dokumentInfo));
		dokumentbeskrivelse.setTilknyttetRegistreringSom(isHoveddokument(rekkefolge) ? TilknyttetRegistreringSom.HOVEDDOKUMENT : TilknyttetRegistreringSom.VEDLEGG);
		dokumentbeskrivelse.setDokumentnummer(BigInteger.valueOf(rekkefolge));
		dokumentbeskrivelse.setTilknyttetDato(datoArkivmeldingOpprettet);
		dokumentbeskrivelse.setTilknyttetAv(journalpostQdist013.getJournalfortAvNavn());
		dokumentbeskrivelse.getDokumentobjekt()
				.add(createAndPopulateDokumentObjekt(journalpostQdist013, dokumentInfo, systemId, isHoveddokument(rekkefolge), objectFactory));
		return dokumentbeskrivelse;
	}

	private boolean isHoveddokument(int rekkefolge) {
		return rekkefolge == 0;
	}

	private String getDokumentOpprettetAv(boolean isHoveddok, JournalpostQdist013 journalpostQdist013, JournalpostQdist013.DokumentInfo dokumentInfo) {
		if (isHoveddok || isEmpty(dokumentInfo.getOriginalJournalpostId())) {
			return journalpostQdist013.getJournalfortAvNavn();
		} else {
			//todo: call Saf to get journalpost.dokumenter.originalJournalpostId→journalfortAvNavn
			return null;
		}
	}

	private Dokumentobjekt createAndPopulateDokumentObjekt(JournalpostQdist013 journalpostQdist013,
														   JournalpostQdist013.DokumentInfo dokumentInfo,
														   String systemId,
														   boolean isHoveddokument,
														   ObjectFactory objectFactory) {
		Dokumentobjekt dokumentobjekt = objectFactory.createDokumentobjekt();
		dokumentobjekt.setVersjonsnummer(BigInteger.ONE);
//		dokumentobjekt.setVariantformat();TODO: MÅ avklares. Filtype må legges til i saf
		dokumentobjekt.setOpprettetDato(convertLocalDateTimeToXmlGregorianCalendar(dokumentInfo.getDatoFerdigstilt()));
		dokumentobjekt.setOpprettetAv(getDokumentOpprettetAv(isHoveddokument, journalpostQdist013, dokumentInfo));
		dokumentobjekt.setReferanseDokumentfil(getReferanseDokumentFil(journalpostQdist013, dokumentInfo));
		return dokumentobjekt;
	}

	private String getReferanseDokumentFil(JournalpostQdist013 journalpostQdist013, JournalpostQdist013.DokumentInfo dokumentInfo) {
		return null;
//		TODO Implement: journalpost.journalpostId + "-" + journalpost.dokumenter.dokumentInfoId + "-" + journalpost.dokumenter.dokumentvariant + "." + journalpost.dokumenter.dokumentvariant.filtype
	}

	private Korrespondansepart createAndPolpulateKorrespondanspartMottaker(JournalpostQdist013 journalpostQdist013, String systemId, ObjectFactory objectFactory) {
		Korrespondansepart korrespondansepart = objectFactory.createKorrespondansepart();
		//TODO
		return korrespondansepart;
	}

	private Korrespondansepart createAndPolpulateKorrespondanspartAvsender(JournalpostQdist013 journalpostQdist013, String systemId, ObjectFactory objectFactory) {
		Korrespondansepart korrespondansepart = objectFactory.createKorrespondansepart();
		//TODO
		return korrespondansepart;
	}

	private Sakspart createAndPopulateSakspartDAP(JournalpostQdist013 journalpostQdist013, ObjectFactory objectFactory) {
		Sakspart sakspartDAP = objectFactory.createSakspart();
		sakspartDAP.setSakspartID(getSakspartIdDAP(journalpostQdist013));
		sakspartDAP.setSakspartNavn(getSakspartNavnDAP(journalpostQdist013));
		sakspartDAP.setSakspartRolle(SAKSPART_ROLLE_DAP);
		return sakspartDAP;
	}

	private String getSakspartIdDAP(JournalpostQdist013 journalpostQdist013) {
		return "TODO";
//		TODO
//		HVIS journalpost.bruker.bruker.type = AKTOERID SÅ sett fødselsnummer hentet fra Aktoer
//		ELLERS sett journalpost.bruker.id
	}

	private String getSakspartNavnDAP(JournalpostQdist013 journalpostQdist013) {
		return "TODO";
//		TODO
//		HVIS journalpost.bruker.type = ORGNR SÅ hent organisasjonens navn fra Enhetsregisteret og sett dette
//		ELLERS hent brukers navn fra TPS og sett dette
	}

	private Sakspart createAndPopulateSakspartAMP(JournalpostQdist013 journalpostQdist013, ObjectFactory objectFactory) {
		Sakspart sakspartAMP = objectFactory.createSakspart();
		sakspartAMP.setSakspartNavn(NAV_KLAGEINSTANS);
		sakspartAMP.setSakspartRolle(SAKSPART_ROLLE_AMP);
		sakspartAMP.setKontaktperson(journalpostQdist013.getOpprettetAvNavn());
		return sakspartAMP;
	}


}
