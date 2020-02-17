package no.nav.dokdisteformidling.qdist013;

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
import no.arkivverket.standarder.noark5.metadatakatalog.Variantformat;
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

import static java.lang.String.format;
import static no.nav.dokdisteformidling.constants.DomainConstants.APP_NAME;
import static no.nav.dokdisteformidling.constants.DomainConstants.VARIANTFORMAT_ARKIV;
import static no.nav.dokdisteformidling.constants.DomainConstants.VARIANTFORMAT_PRODUKSJON;
import static no.nav.dokdisteformidling.constants.DomainConstants.VARIANTFORMAT_SLADDET;
import static no.nav.dokdisteformidling.qdist013.util.ArkivMapperUtil.brukerTypeIsAktoerId;
import static no.nav.dokdisteformidling.qdist013.util.ArkivMapperUtil.brukerTypeIsOrgnr;
import static no.nav.dokdisteformidling.qdist013.util.ArkivMapperUtil.isHoveddokument;
import static no.nav.dokdisteformidling.utils.FunctionalUtils.convertLocalDateTimeToXmlGregorianCalendar;
import static no.nav.dokdisteformidling.utils.FunctionalUtils.generateRandomUUID;
import static no.nav.dokdisteformidling.utils.FunctionalUtils.getNow;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Component
public class ArkivmeldingMapper {

	public static final String NAV_KLAGEINSTANS = "NAV Klageinstans";
	public static final String TRYGDERETTEN = "TRYGDERETTEN";
	public static final String SAKSPART_ROLLE_DAP = "DAP";
	public static final String SAKSPART_ROLLE_AMP = "AMP";
	public static final String INNGAAENDE = "I";
	public static final String UTGAAENDE = "U";
	public static final String DOKUMENTTYPE_DOKUMENTASJON = "Dokumentasjon";
	public static final String FERDIGSTILT = "FERDIGSTILT";
	public static final String FILFORMAT_PNG = "PNG";
	public static final String FILFORMAT_JPEG = "JPEG";
	public static final String UKJENT = "UKJENT";

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
		final String systemId = generateRandomUUID();
		final XMLGregorianCalendar datoArkivmeldingOpprettet = getNow();

		Arkivmelding arkivmelding = objectFactory.createArkivmelding();
		arkivmelding.setSystem(APP_NAME);
		arkivmelding.setMeldingId(bestillingsId);
		arkivmelding.setTidspunkt(datoArkivmeldingOpprettet);
		arkivmelding.setAntallFiler(((int) journalpostQdist013.getDokumenter()
				.stream()
				.filter(dokumentInfo -> isDokumentFerdigstilt(dokumentInfo.getDokumentstatus()))
				.count()));
		arkivmelding.getMappe()
				.add(createAndPopulateSaksmappe(journalpostQdist013, systemId, datoArkivmeldingOpprettet, objectFactory));

		return objectFactory.createArkivmelding(arkivmelding);
	}

	private boolean isDokumentFerdigstilt(String dokumentStatus) {
		return isBlank(dokumentStatus) || FERDIGSTILT.equals(dokumentStatus);
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
					if (isDokumentFerdigstilt(dokumentInfo.getDokumentstatus())) {
						dokumentbeskrivelser.add(createAndPopulateDokumentBeskrivelse(journalpostQdist013, dokumentInfo,
								dokumentbeskrivelser.size() + 1, systemId, datoArkivmeldingOpprettet, objectFactory));
					}
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
		dokumentbeskrivelse.setDokumenttype(DOKUMENTTYPE_DOKUMENTASJON);
		dokumentbeskrivelse.setDokumentstatus(Dokumentstatus.DOKUMENTET_ER_FERDIGSTILT);
		dokumentbeskrivelse.setTittel(getDokumentbeskrivelseTittel(journalpostQdist013, dokumentInfo, isHoveddokument(rekkefolge)));
		dokumentbeskrivelse.setOpprettetDato(getDokumentOpprettetDato(isHoveddokument(rekkefolge), journalpostQdist013, dokumentInfo));
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

	private XMLGregorianCalendar getDokumentOpprettetDato(boolean isHoveddok, JournalpostQdist013 journalpostQdist013, JournalpostQdist013.DokumentInfo dokumentInfo) {
		if (isHoveddok || isEmpty(dokumentInfo.getOriginalJournalpostId())) {
			return convertLocalDateTimeToXmlGregorianCalendar(journalpostQdist013.getDatoJournalfoert());
		} else {
			LightweightSafJournalpostQdist013 lightweightSafJournalpostQdist013 = safJournalpostQueryService.hentJournalpost(dokumentInfo
					.getOriginalJournalpostId());
			return convertLocalDateTimeToXmlGregorianCalendar(lightweightSafJournalpostQdist013.getDatoJournalfoert());
		}
	}

	private String getDokumentOpprettetAv(boolean isHoveddok, JournalpostQdist013 journalpostQdist013, JournalpostQdist013.DokumentInfo dokumentInfo) {
		if (isHoveddok || isEmpty(dokumentInfo.getOriginalJournalpostId())) {
			return journalpostQdist013.getJournalfortAvNavn();
		} else {
			LightweightSafJournalpostQdist013 lightweightSafJournalpostQdist013 = safJournalpostQueryService.hentJournalpost(dokumentInfo
					.getOriginalJournalpostId());
			if (!isEmpty(lightweightSafJournalpostQdist013.getJournalfortAvNavn())) {
				return lightweightSafJournalpostQdist013.getJournalfortAvNavn();
			}
			return UKJENT;
		}
	}

	private Dokumentobjekt createAndPopulateDokumentObjekt(JournalpostQdist013 journalpostQdist013,
														   JournalpostQdist013.DokumentInfo dokumentInfo,
														   boolean isHoveddokument,
														   ObjectFactory objectFactory) {
		Dokumentobjekt dokumentobjekt = objectFactory.createDokumentobjekt();
		dokumentobjekt.setVersjonsnummer(BigInteger.ONE);
		dokumentobjekt.setVariantformat(mapVariantformatSafValueToNoark5VariantFormat(getDokumentVariant(dokumentInfo)));
		dokumentobjekt.setOpprettetDato(getDokumentOpprettetDato(isHoveddokument, journalpostQdist013, dokumentInfo));
		dokumentobjekt.setOpprettetAv(getDokumentOpprettetAv(isHoveddokument, journalpostQdist013, dokumentInfo));
		dokumentobjekt.setReferanseDokumentfil(getReferanseDokumentFil(journalpostQdist013.getJournalpostId(), dokumentInfo));
		return dokumentobjekt;
	}

	private String getReferanseDokumentFil(String journalpostId, JournalpostQdist013.DokumentInfo dokumentInfo) {
		return format("%s-%s-%s-%s", journalpostId, dokumentInfo.getDokumentInfoId(), getDokumentVariant(dokumentInfo), getFiltype(dokumentInfo));
	}

	private Variantformat mapVariantformatSafValueToNoark5VariantFormat(String variantFormatSafValue) {
		switch (variantFormatSafValue) {
			case VARIANTFORMAT_SLADDET:
				return Variantformat.DOKUMENT_HVOR_DELER_AV_INNHOLDET_ER_SKJERMET;
			case VARIANTFORMAT_ARKIV:
				return Variantformat.ARKIVFORMAT;
			case VARIANTFORMAT_PRODUKSJON:
				return Variantformat.PRODUKSJONSFORMAT;
			default:
				return null;
		}
	}

	private String getDokumentVariant(JournalpostQdist013.DokumentInfo dokumentInfo) {
		if (dokumentInfoContainsSladdetDokumentvariant(dokumentInfo)) {
			return VARIANTFORMAT_SLADDET;
		} else {
			JournalpostQdist013.DokumentInfo.Dokumentvariant dokumentvariantArkiv = dokumentInfo.getDokumentvarianter()
					.stream()
					.filter(dokumentvariant -> VARIANTFORMAT_ARKIV.equals(dokumentvariant.getVariantformat()))
					.findAny()
					.get(); //Ok, already validated in SafJournalpostValidatorQdist013.

			return isFiltypePNGorJPEG(dokumentvariantArkiv) ? VARIANTFORMAT_ARKIV : VARIANTFORMAT_PRODUKSJON;
		}
	}

	private boolean dokumentInfoContainsSladdetDokumentvariant(JournalpostQdist013.DokumentInfo dokumentInfo) {
		return dokumentInfo.getDokumentvarianter()
				.stream()
				.anyMatch(dokumentvariant -> VARIANTFORMAT_SLADDET.equals(dokumentvariant.getVariantformat()));
	}

	private String getFiltype(JournalpostQdist013.DokumentInfo dokumentInfo) {
		if (dokumentInfoContainsSladdetDokumentvariant(dokumentInfo)) {
			return dokumentInfo.getDokumentvarianter().stream()
					.filter(dokumentvariant -> VARIANTFORMAT_SLADDET.equals(dokumentvariant.getVariantformat()))
					.findAny()
					.get()
					//Ok, already validated in SafJournalpostValidatorQdist013.
					.getFiltype();
		} else {
			return dokumentInfo.getDokumentvarianter().stream()
					.filter(dokumentvariant -> VARIANTFORMAT_ARKIV.equals(dokumentvariant.getVariantformat()))
					.findAny()
					.get()//Ok, already validated in SafJournalpostValidatorQdist013.
					.getFiltype();
		}
	}

	private boolean isFiltypePNGorJPEG(JournalpostQdist013.DokumentInfo.Dokumentvariant dokumentvariant) {
		return FILFORMAT_JPEG.equals(dokumentvariant.getFiltype()) || FILFORMAT_PNG.equals(dokumentvariant.getFiltype());
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
