package no.nav.dokdisteformidling.qdist013;

import static java.lang.String.format;
import static no.nav.dokdisteformidling.common.FunctionalUtils.convertLocalDateTimeToXmlGregorianCalendar;
import static no.nav.dokdisteformidling.common.FunctionalUtils.generateRandomUUID;
import static no.nav.dokdisteformidling.common.FunctionalUtils.getNow;
import static no.nav.dokdisteformidling.constants.DomainConstants.APP_NAME;
import static no.nav.dokdisteformidling.qdist013.util.ArkivMapperUtil.brukerTypeIsAktoerId;
import static no.nav.dokdisteformidling.qdist013.util.ArkivMapperUtil.brukerTypeIsOrgnr;
import static no.nav.dokdisteformidling.qdist013.util.ArkivMapperUtil.isHoveddokument;
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
import no.arkivverket.standarder.noark5.metadatakatalog.Korrespondanseparttype;
import no.arkivverket.standarder.noark5.metadatakatalog.Saksstatus;
import no.arkivverket.standarder.noark5.metadatakatalog.TilknyttetRegistreringSom;
import no.nav.dokdisteformidling.consumer.aktoerregister.Aktoerregister;
import no.nav.dokdisteformidling.consumer.ereg.Ereg;
import no.nav.dokdisteformidling.consumer.saf.SafJournalpostQueryService;
import no.nav.dokdisteformidling.consumer.tps.Tps;
import no.nav.dokdisteformidling.qdist013.saf.lightweight.LightweightSafJournalpostQdist013;
import no.nav.dokdisteformidling.qdist013.saf.main.JournalpostQdist013;
import org.springframework.stereotype.Component;

import javax.inject.Named;
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

	private static final String NAV_KLAGEINSTANS = "NAV Klageinstans";
	private static final String TRYGDERETTEN = "TRYGDERETTEN";
	private static final String SAKSPART_ROLLE_DAP = "DAP";
	private static final String SAKSPART_ROLLE_AMP = "AMP";
	private static final String INNGAAENDE = "I";
	private static final String UTGAAENDE = "U";

	private final Aktoerregister aktoerregister;
	private final Ereg ereg;
	private final Tps tps;
	private final SafJournalpostQueryService<LightweightSafJournalpostQdist013> safJournalpostQueryService;

	public ArkivmeldingMapper(@Named("LightweightSafJournalpostQueryServiceQdist013") SafJournalpostQueryService<LightweightSafJournalpostQdist013> safJournalpostQueryService,
							  Aktoerregister aktoerregister,
							  Ereg ereg,
							  Tps tps) {
		this.safJournalpostQueryService = safJournalpostQueryService;
		this.aktoerregister = aktoerregister;
		this.ereg = ereg;
		this.tps = tps;
	}

	public JAXBElement<Arkivmelding> createArkivMelding(JournalpostQdist013 journalpostQdist013, String bestillingsId) {
		ObjectFactory objectFactory = new ObjectFactory();
		final String systemId = generateRandomUUID(); //TODO: Korrekt å bruke samme systemId over alt?
		final XMLGregorianCalendar datoArkivmeldingOpprettet = getNow();

		Arkivmelding arkivmelding = objectFactory.createArkivmelding();
		arkivmelding.setSystem(APP_NAME);
		arkivmelding.setMeldingId(bestillingsId);
		arkivmelding.setTidspunkt(datoArkivmeldingOpprettet);
		arkivmelding.setAntallFiler(journalpostQdist013.getDokumenter().size());
		arkivmelding.getMappe()
				.add(createAndPopulateSaksmappe(journalpostQdist013, systemId, datoArkivmeldingOpprettet, objectFactory));

		return objectFactory.createArkivmelding(arkivmelding);
	}

	private Saksmappe createAndPopulateSaksmappe(JournalpostQdist013 journalpostQdist013,
												 String systemId,
												 XMLGregorianCalendar datoArkivmeldingOpprettet,
												 ObjectFactory objectFactory) {
		Saksmappe saksmappe = objectFactory.createSaksmappe();
		saksmappe.setSystemID(systemId);
		saksmappe.setTittel(journalpostQdist013.getTemanavn());
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
		journalpost.getKorrespondansepart().add(createAndPolpulateKorrespondanspartMottaker(objectFactory));
		journalpost.getKorrespondansepart().add(createAndPolpulateKorrespondanspartAvsender(objectFactory));
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
		dokumentbeskrivelse.setTittel(getDokumentbeskrivelseTittel(journalpostQdist013, dokumentInfo, isHoveddokument(rekkefolge)));
		dokumentbeskrivelse.setOpprettetDato(convertLocalDateTimeToXmlGregorianCalendar(dokumentInfo.getDatoFerdigstilt()));
		dokumentbeskrivelse.setOpprettetAv(getDokumentOpprettetAv(isHoveddokument(rekkefolge), journalpostQdist013, dokumentInfo));
		dokumentbeskrivelse.setTilknyttetRegistreringSom(isHoveddokument(rekkefolge) ? TilknyttetRegistreringSom.HOVEDDOKUMENT : TilknyttetRegistreringSom.VEDLEGG);
		dokumentbeskrivelse.setDokumentnummer(BigInteger.valueOf(rekkefolge));
		dokumentbeskrivelse.setTilknyttetDato(datoArkivmeldingOpprettet);
		dokumentbeskrivelse.setTilknyttetAv(journalpostQdist013.getJournalfortAvNavn());
		dokumentbeskrivelse.getDokumentobjekt()
				.add(createAndPopulateDokumentObjekt(journalpostQdist013, dokumentInfo, isHoveddokument(rekkefolge), objectFactory));
		return dokumentbeskrivelse;
	}

	private String getDokumentbeskrivelseTittel(JournalpostQdist013 journalpostQdist013, JournalpostQdist013.DokumentInfo dokumentInfo, boolean isHoveddok) {
		if (isHoveddok || dokumentInfo.getOriginalJournalpostId() == null) {
			return dokumentInfo.getTittel();
		} else {
			if (INNGAAENDE.equals(journalpostQdist013.getJournalposttype())) {
				return format("%s, Fra %s", dokumentInfo.getTittel(), getAvsenderMottakerNavn(dokumentInfo.getOriginalJournalpostId()));
			} else if (UTGAAENDE.equals(journalpostQdist013.getJournalposttype())) {
				return format("%s, Til %s", dokumentInfo.getTittel(), getAvsenderMottakerNavn(dokumentInfo.getOriginalJournalpostId()));
			} else {
				return dokumentInfo.getTittel();
			}
		}
	}

	private String getAvsenderMottakerNavn(String jounalpostId) {
		return safJournalpostQueryService.hentJournalpost(jounalpostId).getAvsenderMottakerNavn();

	}

	private String getDokumentOpprettetAv(boolean isHoveddok, JournalpostQdist013 journalpostQdist013, JournalpostQdist013.DokumentInfo dokumentInfo) {
		if (isHoveddok || isEmpty(dokumentInfo.getOriginalJournalpostId())) {
			return journalpostQdist013.getJournalfortAvNavn();
		} else {
			LightweightSafJournalpostQdist013 lightweightSafJournalpostQdist013 = safJournalpostQueryService.hentJournalpost(dokumentInfo
					.getOriginalJournalpostId());
			return lightweightSafJournalpostQdist013.getJournalfortAvNavn();
		}
	}

	private Dokumentobjekt createAndPopulateDokumentObjekt(JournalpostQdist013 journalpostQdist013,
														   JournalpostQdist013.DokumentInfo dokumentInfo,
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
//		TODO Filtype må legges til i saf
	}

	private Korrespondansepart createAndPolpulateKorrespondanspartAvsender(ObjectFactory objectFactory) {
		Korrespondansepart korrespondansepartAvsender = objectFactory.createKorrespondansepart();
		korrespondansepartAvsender.setKorrespondanseparttype(Korrespondanseparttype.AVSENDER);
		korrespondansepartAvsender.setKorrespondansepartNavn(NAV_KLAGEINSTANS); //TODO Må avklares. Skal kanskje bare være "NAV"
		return korrespondansepartAvsender;
	}

	private Korrespondansepart createAndPolpulateKorrespondanspartMottaker(ObjectFactory objectFactory) {
		Korrespondansepart korrespondansepartMottaker = objectFactory.createKorrespondansepart();
		korrespondansepartMottaker.setKorrespondanseparttype(Korrespondanseparttype.MOTTAKER);
		korrespondansepartMottaker.setKorrespondansepartNavn(TRYGDERETTEN);
		return korrespondansepartMottaker;
	}

	private Sakspart createAndPopulateSakspartDAP(JournalpostQdist013 journalpostQdist013, ObjectFactory objectFactory) {
		Sakspart sakspartDAP = objectFactory.createSakspart();
		final String fnrObtainedFromAktoerId = getFnrIfBrukerTypeIsAktoerIdElseReturnNull(journalpostQdist013);
		sakspartDAP.setSakspartID(getSakspartIdDAP(journalpostQdist013, fnrObtainedFromAktoerId));
		sakspartDAP.setSakspartNavn(getSakspartNavnDAP(journalpostQdist013, fnrObtainedFromAktoerId));
		sakspartDAP.setSakspartRolle(SAKSPART_ROLLE_DAP);
		return sakspartDAP;
	}

	private String getFnrIfBrukerTypeIsAktoerIdElseReturnNull(JournalpostQdist013 journalpostQdist013) {
		if (brukerTypeIsAktoerId(journalpostQdist013)) {
			return aktoerregister.hentIdentForAktoerId(journalpostQdist013.getBruker().getId());
		} else {
			return null;
		}
	}

	private String getSakspartIdDAP(JournalpostQdist013 journalpostQdist013, String fnrObtainedFromAktoerId) {
		if (brukerTypeIsAktoerId(journalpostQdist013)) {
			return fnrObtainedFromAktoerId;
		} else {
			return journalpostQdist013.getBruker().getId();
		}
	}

	private String getSakspartNavnDAP(JournalpostQdist013 journalpostQdist013, String fnrObtainedFromAktoerId) {
		if (brukerTypeIsOrgnr(journalpostQdist013)) {
			return ereg.hentNavn(journalpostQdist013.getBruker().getId());
		} else if (brukerTypeIsAktoerId(journalpostQdist013)) {
			return tps.hentNavn(fnrObtainedFromAktoerId);
		} else {
			return tps.hentNavn(journalpostQdist013.getBruker().getId());
		}
	}

	private Sakspart createAndPopulateSakspartAMP(JournalpostQdist013 journalpostQdist013, ObjectFactory objectFactory) {
		Sakspart sakspartAMP = objectFactory.createSakspart();
		sakspartAMP.setSakspartNavn(NAV_KLAGEINSTANS);
		sakspartAMP.setSakspartRolle(SAKSPART_ROLLE_AMP);
		sakspartAMP.setKontaktperson(journalpostQdist013.getOpprettetAvNavn());
		return sakspartAMP;
	}


}
