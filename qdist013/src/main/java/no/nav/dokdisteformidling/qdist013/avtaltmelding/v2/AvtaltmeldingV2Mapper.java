package no.nav.dokdisteformidling.qdist013.avtaltmelding.v2;

import jakarta.xml.bind.JAXBElement;
import no.arkivverket.standarder.noark5.arkivmelding.v2.Arkivmelding;
import no.arkivverket.standarder.noark5.arkivmelding.v2.Dokumentbeskrivelse;
import no.arkivverket.standarder.noark5.arkivmelding.v2.Dokumentobjekt;
import no.arkivverket.standarder.noark5.arkivmelding.v2.EnhetsidentifikatorType;
import no.arkivverket.standarder.noark5.arkivmelding.v2.FoedselsnummerType;
import no.arkivverket.standarder.noark5.arkivmelding.v2.Journalpost;
import no.arkivverket.standarder.noark5.arkivmelding.v2.Korrespondansepart;
import no.arkivverket.standarder.noark5.arkivmelding.v2.ObjectFactory;
import no.arkivverket.standarder.noark5.arkivmelding.v2.Part;
import no.arkivverket.standarder.noark5.arkivmelding.v2.Saksmappe;
import no.nav.avtaltmelding.trygderetten.v1.NavMappe;
import no.nav.dokdisteformidling.consumer.ereg.EregConsumer;
import no.nav.dokdisteformidling.consumer.pdl.PdlGraphQLConsumer;
import no.nav.dokdisteformidling.consumer.saf.SafJournalpostQueryService;
import no.nav.dokdisteformidling.qdist013.saf.lightweight.LightweightSafJournalpostQdist013;
import no.nav.dokdisteformidling.qdist013.saf.main.JournalpostQdist013;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static java.lang.String.format;
import static java.math.BigInteger.ONE;
import static no.nav.dokdisteformidling.constants.DomainConstants.APP_NAME;
import static no.nav.dokdisteformidling.constants.DomainConstants.VARIANTFORMAT_ARKIV;
import static no.nav.dokdisteformidling.constants.DomainConstants.VARIANTFORMAT_SLADDET;
import static no.nav.dokdisteformidling.qdist013.util.AvtaltmeldingConstant.NAV_KLAGEINSTANS_STYRINGSENHETEN_ORGNUMMER;
import static no.nav.dokdisteformidling.qdist013.util.AvtaltmeldingConstant.TRYGDERETTEN_ORGNUMMER;
import static no.nav.dokdisteformidling.qdist013.saf.main.JournalpostQdist013.DokumentInfo.Dokumentvariant.FILTYPE_JPEG;
import static no.nav.dokdisteformidling.qdist013.saf.main.JournalpostQdist013.DokumentInfo.Dokumentvariant.FILTYPE_PNG;
import static no.nav.dokdisteformidling.qdist013.util.ArkivMapperUtil.brukerTypeIsAktoerId;
import static no.nav.dokdisteformidling.qdist013.util.ArkivMapperUtil.brukerTypeIsOrgnr;
import static no.nav.dokdisteformidling.qdist013.util.ArkivMapperUtil.isBrukerTypeFnr;
import static no.nav.dokdisteformidling.qdist013.util.ArkivMapperUtil.isHoveddokument;
import static no.nav.dokdisteformidling.qdist013.util.AvtaltmeldingConstant.ARKIVFORMAT;
import static no.nav.dokdisteformidling.qdist013.util.AvtaltmeldingConstant.AVSENDER;
import static no.nav.dokdisteformidling.qdist013.util.AvtaltmeldingConstant.DOKUMENTASJON;
import static no.nav.dokdisteformidling.qdist013.util.AvtaltmeldingConstant.DOKUMENTET_ER_FERDIGSTILT;
import static no.nav.dokdisteformidling.qdist013.util.AvtaltmeldingConstant.DOKUMENT_HVOR_DELER_AV_INNHOLDET_ER_SKJERMET;
import static no.nav.dokdisteformidling.qdist013.util.AvtaltmeldingConstant.EKSPEDERT;
import static no.nav.dokdisteformidling.qdist013.util.AvtaltmeldingConstant.HOVEDDOKUMENT;
import static no.nav.dokdisteformidling.qdist013.util.AvtaltmeldingConstant.MOTTAKER;
import static no.nav.dokdisteformidling.qdist013.util.AvtaltmeldingConstant.PRODUKSJONSFORMAT;
import static no.nav.dokdisteformidling.qdist013.util.AvtaltmeldingConstant.UNDER_BEHANDLING;
import static no.nav.dokdisteformidling.qdist013.util.AvtaltmeldingConstant.UTGAAENDE_DOKUMENT;
import static no.nav.dokdisteformidling.qdist013.util.AvtaltmeldingConstant.VEDLEGG;
import static no.nav.dokdisteformidling.utils.DateConverterUtil.convertLocalDateTimeToXmlGregorianCalendar;
import static no.nav.dokdisteformidling.utils.DateConverterUtil.getNow;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Profile("avtaltmelding_v2")
@Component
public class AvtaltmeldingV2Mapper {

	static final String NAV_KLAGEINSTANS = "NAV Klageinstans";
	static final String TRYGDERETTEN = "TRYGDERETTEN";
	static final String SAKSPART_ROLLE_DAP = "DAP";
	static final String SAKSPART_ROLLE_AMP = "AMP";
	static final String INNGAAENDE = "I";
	static final String UTGAAENDE = "U";
	static final String FERDIGSTILT = "FERDIGSTILT";
	static final String UKJENT = "UKJENT";
	static final String AVTALTMELDING_NAMESPACE = "http://www.arkivverket.no/standarder/noark5/arkivmelding";
	static final String REFERANSE_DOKUMENTFIL_FORMAT = "%s-%s-%s.%s";

	private final PdlGraphQLConsumer pdlGraphQLConsumer;
	private final EregConsumer eregConsumer;
	private final SafJournalpostQueryService<LightweightSafJournalpostQdist013> safJournalpostQueryService;

	public AvtaltmeldingV2Mapper(@Qualifier("LightweightSafJournalpostQueryServiceQdist013") SafJournalpostQueryService<LightweightSafJournalpostQdist013> safJournalpostQueryService,
								 EregConsumer eregConsumer,
								 PdlGraphQLConsumer pdlGraphQLConsumer) {
		this.safJournalpostQueryService = safJournalpostQueryService;
		this.eregConsumer = eregConsumer;
		this.pdlGraphQLConsumer = pdlGraphQLConsumer;
	}

	public JAXBElement<Arkivmelding> createArkivMelding(JournalpostQdist013 journalpostQdist013, String bestillingsId) {
		ObjectFactory objectFactory = new ObjectFactory();
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
				.add(createAndPopulateSaksmappe(journalpostQdist013, datoArkivmeldingOpprettet, objectFactory));

		return objectFactory.createArkivmelding(arkivmelding);
	}

	private boolean isDokumentFerdigstilt(String dokumentStatus) {
		return isBlank(dokumentStatus) || FERDIGSTILT.equals(dokumentStatus);
	}

	private Saksmappe createAndPopulateSaksmappe(JournalpostQdist013 journalpostQdist013,
												 XMLGregorianCalendar datoArkivmeldingOpprettet,
												 ObjectFactory objectFactory) {
		Journalpost journalpost = mapJournalpost(journalpostQdist013, datoArkivmeldingOpprettet, objectFactory);
		XMLGregorianCalendar datoOpprettet = mapOpprettetDato(journalpostQdist013, journalpost);

		Saksmappe saksmappe = objectFactory.createSaksmappe();
		saksmappe.setTittel(journalpostQdist013.getTemanavn());
		saksmappe.setOpprettetDato(datoOpprettet);
		saksmappe.setOpprettetAv(journalpostQdist013.getOpprettetAvNavn());
		saksmappe.setVirksomhetsspesifikkeMetadata(mapNavMappe(journalpostQdist013.getSak().getArkivsaksnummer()));
		saksmappe.getPart()
				.add(createAndPopulatePartAMP(journalpostQdist013, objectFactory));
		saksmappe.getPart()
				.add(createAndPopulatePartDAP(journalpostQdist013, objectFactory));
		saksmappe.getRegistrering().add(journalpost);
		saksmappe.setSaksdato(datoOpprettet);
		saksmappe.setAdministrativEnhet(NAV_KLAGEINSTANS);
		saksmappe.setSaksansvarlig(journalpostQdist013.getOpprettetAvNavn());
		saksmappe.setJournalenhet(journalpostQdist013.getJournalfoerendeEnhet());
		saksmappe.setSaksstatus(UNDER_BEHANDLING);

		return saksmappe;
	}

	private static JAXBElement<JAXBElement> mapNavMappe(String arkivsaksnummer) {
		NavMappe navMappe = new NavMappe();
		navMappe.setSaksnummer(arkivsaksnummer);
		JAXBElement<NavMappe> navMappeElement = new no.nav.avtaltmelding.trygderetten.v1.ObjectFactory().createNavMappe(navMappe);
		return new JAXBElement<>(new QName(AVTALTMELDING_NAMESPACE, "virksomhetsspesifikkeMetadata"), JAXBElement.class, navMappeElement);
	}

	private static XMLGregorianCalendar mapOpprettetDato(JournalpostQdist013 journalpostQdist013, Journalpost journalpost) {
		LocalDateTime opprettetDato = journalpostQdist013.getSak().getDatoOpprettet();
		if (opprettetDato == null) {
			return finnEldsteVedleggSortertEtterDokumentbeskrivelseOpprettetDato(journalpost);
		}
		return convertLocalDateTimeToXmlGregorianCalendar(opprettetDato);
	}

	private static XMLGregorianCalendar finnEldsteVedleggSortertEtterDokumentbeskrivelseOpprettetDato(Journalpost journalpost) {
		XMLGregorianCalendar eldstedato = null;
		for (Dokumentbeskrivelse dokumentbeskrivelse : journalpost.getDokumentbeskrivelse()) {
			if (dokumentbeskrivelse.getTilknyttetRegistreringSom().equals(VEDLEGG)) {
				if (eldstedato == null || dokumentbeskrivelse.getOpprettetDato().compare(eldstedato) == DatatypeConstants.LESSER) {
					eldstedato = dokumentbeskrivelse.getOpprettetDato();
				}
			}
		}
		return eldstedato;
	}

	private Journalpost mapJournalpost(JournalpostQdist013 journalpostQdist013, XMLGregorianCalendar datoArkivmeldingOpprettet, ObjectFactory objectFactory) {
		Journalpost journalpost = objectFactory.createJournalpost();
		journalpost.setOpprettetDato(convertLocalDateTimeToXmlGregorianCalendar(journalpostQdist013.getDatoOpprettet()));
		journalpost.setOpprettetAv(journalpostQdist013.getOpprettetAvNavn());
		addDokumentBeskrivelserToJournalpost(journalpost, journalpostQdist013, datoArkivmeldingOpprettet, objectFactory);
		journalpost.setTittel(journalpostQdist013.getTittel());
		journalpost.getKorrespondansepart().add(createKorrespondansepartMottaker(objectFactory));
		journalpost.getKorrespondansepart().add(createKorrespondansepartAvsender(objectFactory));
		journalpost.setJournalposttype(UTGAAENDE_DOKUMENT);
		journalpost.setJournalstatus(EKSPEDERT);
		journalpost.setJournaldato(convertLocalDateTimeToXmlGregorianCalendar(journalpostQdist013.getDatoJournalfoert()));
		return journalpost;
	}

	private void addDokumentBeskrivelserToJournalpost(Journalpost journalpost,
													  JournalpostQdist013 journalpostQdist013,
													  XMLGregorianCalendar avtalemeldingOprettetDato,
													  ObjectFactory objectFactory) {
		List<Dokumentbeskrivelse> dokumentbeskrivelser = journalpost.getDokumentbeskrivelse();
		journalpostQdist013.getDokumenter()
				.forEach(dokumentInfo -> {
					if (isDokumentFerdigstilt(dokumentInfo.getDokumentstatus())) {
						dokumentbeskrivelser.add(createAndPopulateDokumentBeskrivelse(journalpostQdist013, dokumentInfo,
								dokumentbeskrivelser.size() + 1, avtalemeldingOprettetDato, objectFactory));
					}

				});
	}


	private Dokumentbeskrivelse createAndPopulateDokumentBeskrivelse(JournalpostQdist013 journalpostQdist013,
																	 JournalpostQdist013.DokumentInfo dokumentInfo,
																	 int rekkefolge,
																	 XMLGregorianCalendar datoArkivmeldingOpprettet,
																	 ObjectFactory objectFactory) {
		Dokumentbeskrivelse dokumentbeskrivelse = objectFactory.createDokumentbeskrivelse();

		dokumentbeskrivelse.setDokumenttype(DOKUMENTASJON);
		dokumentbeskrivelse.setDokumentstatus(DOKUMENTET_ER_FERDIGSTILT);
		dokumentbeskrivelse.setTittel(getDokumentbeskrivelseTittel(dokumentInfo, isHoveddokument(rekkefolge)));
		dokumentbeskrivelse.setOpprettetDato(getDokumentDatoJournalfoert(isHoveddokument(rekkefolge), journalpostQdist013, dokumentInfo));
		dokumentbeskrivelse.setOpprettetAv(getDokumentJournalfortAvNavn(isHoveddokument(rekkefolge), journalpostQdist013, dokumentInfo));
		dokumentbeskrivelse.setTilknyttetRegistreringSom(isHoveddokument(rekkefolge) ? HOVEDDOKUMENT : VEDLEGG);
		dokumentbeskrivelse.setDokumentnummer(BigInteger.valueOf(rekkefolge));
		dokumentbeskrivelse.setTilknyttetDato(datoArkivmeldingOpprettet);
		dokumentbeskrivelse.setTilknyttetAv(journalpostQdist013.getJournalfortAvNavn());
		dokumentbeskrivelse.getDokumentobjekt()
				.add(createAndPopulateDokumentObjekt(journalpostQdist013, dokumentInfo, isHoveddokument(rekkefolge), objectFactory));

		return dokumentbeskrivelse;
	}

	private String getDokumentbeskrivelseTittel(JournalpostQdist013.DokumentInfo dokumentInfo, boolean isHoveddok) {
		if (!isHoveddok && isNotBlank(dokumentInfo.getOriginalJournalpostId())) {
			if (INNGAAENDE.equals(getJournalpostType(dokumentInfo.getOriginalJournalpostId()))) {
				return format("%s, Fra %s", dokumentInfo.getTittel(), getAvsenderMottakerNavn(dokumentInfo.getOriginalJournalpostId()));
			} else if (UTGAAENDE.equals(getJournalpostType(dokumentInfo.getOriginalJournalpostId()))) {
				return format("%s, Til %s", dokumentInfo.getTittel(), getAvsenderMottakerNavn(dokumentInfo.getOriginalJournalpostId()));
			} else {
				return dokumentInfo.getTittel();
			}

		} else {
			return dokumentInfo.getTittel();
		}
	}

	private String getJournalpostType(String originalJournalpostId) {
		return getLightweightSafJournalpost(originalJournalpostId) == null ? null : Objects.requireNonNull(getLightweightSafJournalpost(originalJournalpostId)).getJournalposttype();
	}

	private String getAvsenderMottakerNavn(String journalpostId) {
		return getLightweightSafJournalpost(journalpostId) == null ? null : getLightweightSafJournalpost(journalpostId).getAvsenderMottakerNavn();
	}

	private LightweightSafJournalpostQdist013 getLightweightSafJournalpost(String journalpostId) {
		return isBlank(journalpostId) ? null : safJournalpostQueryService.hentJournalpost(journalpostId);
	}

	private XMLGregorianCalendar getDokumentDatoJournalfoert(boolean isHoveddok, JournalpostQdist013 journalpostQdist013, JournalpostQdist013.DokumentInfo dokumentInfo) {
		if (!isHoveddok && isNotBlank(dokumentInfo.getOriginalJournalpostId()) && !isJournalDatoNull(dokumentInfo.getOriginalJournalpostId())) {
			LightweightSafJournalpostQdist013 lightweightSafJournalpostQdist013 = safJournalpostQueryService.hentJournalpost(dokumentInfo
					.getOriginalJournalpostId());
			return convertLocalDateTimeToXmlGregorianCalendar(lightweightSafJournalpostQdist013.getDatoJournalfoert());
		} else {
			return convertLocalDateTimeToXmlGregorianCalendar(journalpostQdist013.getDatoJournalfoert());
		}
	}

	private boolean isJournalDatoNull(String journalpostId) {
		return getLightweightSafJournalpost(journalpostId) == null ? Objects.isNull(getLightweightSafJournalpost(journalpostId)) : getLightweightSafJournalpost(journalpostId).getDatoJournalfoert() == null;
	}

	private String getDokumentJournalfortAvNavn(boolean isHoveddok, JournalpostQdist013 journalpostQdist013, JournalpostQdist013.DokumentInfo dokumentInfo) {
		if (!isHoveddok && isNotBlank(dokumentInfo.getOriginalJournalpostId())) {
			LightweightSafJournalpostQdist013 lightweightSafJournalpostQdist013 = getLightweightSafJournalpost(dokumentInfo.getOriginalJournalpostId());
			if (!isJournalfortAvNavnNull(dokumentInfo.getOriginalJournalpostId())) {
				return lightweightSafJournalpostQdist013.getJournalfortAvNavn();
			}
			return UKJENT;

		} else {
			return journalpostQdist013.getJournalfortAvNavn();
		}
	}

	private boolean isJournalfortAvNavnNull(String journalpostId) {
		return getLightweightSafJournalpost(journalpostId) == null ? Objects.isNull(getLightweightSafJournalpost(journalpostId)) : isBlank(getLightweightSafJournalpost(journalpostId).getJournalfortAvNavn());
	}


	private Dokumentobjekt createAndPopulateDokumentObjekt(JournalpostQdist013 journalpostQdist013,
														   JournalpostQdist013.DokumentInfo dokumentInfo,
														   boolean isHoveddokument,
														   ObjectFactory objectFactory) {
		Dokumentobjekt dokumentobjekt = objectFactory.createDokumentobjekt();
		dokumentobjekt.setVersjonsnummer(ONE);
		AvtaltFilformat avtaltFilformat = AvtaltFilformatMapper.map(dokumentInfo);
		dokumentobjekt.setVariantformat(getDokumentVariant(dokumentInfo));
		dokumentobjekt.setFormat(avtaltFilformat.getFormat());
		dokumentobjekt.setOpprettetDato(getDokumentDatoJournalfoert(isHoveddokument, journalpostQdist013, dokumentInfo));
		dokumentobjekt.setOpprettetAv(getDokumentJournalfortAvNavn(isHoveddokument, journalpostQdist013, dokumentInfo));
		dokumentobjekt.setReferanseDokumentfil(mapReferanseDokumentfil(journalpostQdist013.getJournalpostId(), dokumentInfo, avtaltFilformat));
		return dokumentobjekt;
	}

	private String mapReferanseDokumentfil(String journalpostId, JournalpostQdist013.DokumentInfo dokumentInfo, AvtaltFilformat avtaltFilformat) {
		return format(REFERANSE_DOKUMENTFIL_FORMAT, journalpostId, dokumentInfo.getDokumentInfoId(), getDokumentVariant(dokumentInfo), avtaltFilformat.getFilendelse());
	}

	private String getDokumentVariant(JournalpostQdist013.DokumentInfo dokumentInfo) {
		if (dokumentInfoContainsSladdetDokumentvariant(dokumentInfo)) {
			return DOKUMENT_HVOR_DELER_AV_INNHOLDET_ER_SKJERMET;
		} else {
			JournalpostQdist013.DokumentInfo.Dokumentvariant dokumentvariantArkiv = dokumentInfo.getDokumentvarianter()
					.stream()
					.filter(dokumentvariant -> VARIANTFORMAT_ARKIV.equals(dokumentvariant.getVariantformat()))
					.findAny()
					.get(); //Ok, already validated in SafJournalpostValidatorQdist013.

			return isFiltypePNGorJPEG(dokumentvariantArkiv) ? ARKIVFORMAT : PRODUKSJONSFORMAT;
		}
	}

	static boolean dokumentInfoContainsSladdetDokumentvariant(JournalpostQdist013.DokumentInfo dokumentInfo) {
		return dokumentInfo.getDokumentvarianter()
				.stream()
				.anyMatch(dokumentvariant -> VARIANTFORMAT_SLADDET.equals(dokumentvariant.getVariantformat()));
	}

	private boolean isFiltypePNGorJPEG(JournalpostQdist013.DokumentInfo.Dokumentvariant dokumentvariant) {
		return FILTYPE_JPEG.equals(dokumentvariant.getFiltype()) || FILTYPE_PNG.equals(dokumentvariant.getFiltype());
	}

	private Korrespondansepart createKorrespondansepartAvsender(ObjectFactory objectFactory) {
		Korrespondansepart korrespondansepartAvsender = objectFactory.createKorrespondansepart();
		korrespondansepartAvsender.setKorrespondanseparttype(AVSENDER);
		korrespondansepartAvsender.setKorrespondansepartNavn(NAV_KLAGEINSTANS);
		korrespondansepartAvsender.setOrganisasjonsnummer(new EnhetsidentifikatorType()
				.useOrganisasjonsnummer(NAV_KLAGEINSTANS_STYRINGSENHETEN_ORGNUMMER));
		return korrespondansepartAvsender;
	}

	private Korrespondansepart createKorrespondansepartMottaker(ObjectFactory objectFactory) {
		Korrespondansepart korrespondansepartMottaker = objectFactory.createKorrespondansepart();
		korrespondansepartMottaker.setKorrespondanseparttype(MOTTAKER);
		korrespondansepartMottaker.setKorrespondansepartNavn(TRYGDERETTEN);
		korrespondansepartMottaker.setOrganisasjonsnummer(new EnhetsidentifikatorType()
				.useOrganisasjonsnummer(TRYGDERETTEN_ORGNUMMER)
		);
		return korrespondansepartMottaker;
	}

	private Part createAndPopulatePartDAP(JournalpostQdist013 journalpostQdist013, ObjectFactory objectFactory) {
		Part partDAP = objectFactory.createPart();
		partDAP.setPartNavn(getSakspartNavnDAP(journalpostQdist013));
		partDAP.setPartRolle(SAKSPART_ROLLE_DAP);

		if (brukerTypeIsOrgnr(journalpostQdist013)) {
			partDAP.setOrganisasjonsnummer(new EnhetsidentifikatorType()
					.useOrganisasjonsnummer(hentOrgNummerDAP(journalpostQdist013)));
		} else {
			partDAP.setFoedselsnummer(new FoedselsnummerType()
					.useFoedselsnummer(getFoedselsnummer(journalpostQdist013)));
		}
		return partDAP;
	}

	private Part createAndPopulatePartAMP(JournalpostQdist013 journalpostQdist013, ObjectFactory objectFactory) {
		Part partAMP = objectFactory.createPart();
		partAMP.setPartNavn(NAV_KLAGEINSTANS);
		partAMP.setPartRolle(SAKSPART_ROLLE_AMP);
		partAMP.setOrganisasjonsnummer(new EnhetsidentifikatorType()
				.useOrganisasjonsnummer(NAV_KLAGEINSTANS_STYRINGSENHETEN_ORGNUMMER));
		partAMP.setKontaktperson(journalpostQdist013.getOpprettetAvNavn());
		return partAMP;
	}

	private String getFoedselsnummer(JournalpostQdist013 journalpostQdist013) {
		if (brukerTypeIsAktoerId(journalpostQdist013)) {
			return pdlGraphQLConsumer.hentPerson(journalpostQdist013.getBruker().getId()).getIdent();
		} else if (isBrukerTypeFnr(journalpostQdist013)) {
			return journalpostQdist013.getBruker().getId();
		} else {
			return null;
		}
	}

	private String hentOrgNummerDAP(JournalpostQdist013 journalpostQdist013) {
		return brukerTypeIsOrgnr(journalpostQdist013) ? journalpostQdist013.getBruker().getId() : null;
	}

	private String getSakspartNavnDAP(JournalpostQdist013 journalpostQdist013) {
		var brukerId = journalpostQdist013.getBruker().getId();

		if (brukerTypeIsOrgnr(journalpostQdist013)) {
			return eregConsumer.hentOrganisasjonsnavn(brukerId);
		} else {
			return pdlGraphQLConsumer.hentPerson(brukerId).getFulltnavn();
		}
	}
}
