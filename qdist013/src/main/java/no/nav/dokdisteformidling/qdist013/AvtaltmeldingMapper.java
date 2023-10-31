package no.nav.dokdisteformidling.qdist013;

import no.arkivverket.standarder.noark5.arkivmelding.Arkivmelding;
import no.arkivverket.standarder.noark5.arkivmelding.Dokumentbeskrivelse;
import no.arkivverket.standarder.noark5.arkivmelding.Dokumentobjekt;
import no.arkivverket.standarder.noark5.arkivmelding.EnhetsidentifikatorType;
import no.arkivverket.standarder.noark5.arkivmelding.FoedselsnummerType;
import no.arkivverket.standarder.noark5.arkivmelding.Journalpost;
import no.arkivverket.standarder.noark5.arkivmelding.Korrespondansepart;
import no.arkivverket.standarder.noark5.arkivmelding.ObjectFactory;
import no.arkivverket.standarder.noark5.arkivmelding.Part;
import no.arkivverket.standarder.noark5.arkivmelding.Saksmappe;
import no.nav.dokdisteformidling.consumer.ereg.Ereg;
import no.nav.dokdisteformidling.consumer.pdl.HentPersonInfo;
import no.nav.dokdisteformidling.consumer.pdl.PdlGraphQLConsumer;
import no.nav.dokdisteformidling.consumer.saf.SafJournalpostQueryService;
import no.nav.dokdisteformidling.qdist013.saf.lightweight.LightweightSafJournalpostQdist013;
import no.nav.dokdisteformidling.qdist013.saf.main.JournalpostQdist013;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

import static java.lang.String.format;
import static java.math.BigInteger.ONE;
import static no.nav.dokdisteformidling.constants.DomainConstants.APP_NAME;
import static no.nav.dokdisteformidling.constants.DomainConstants.VARIANTFORMAT_ARKIV;
import static no.nav.dokdisteformidling.constants.DomainConstants.VARIANTFORMAT_SLADDET;
import static no.nav.dokdisteformidling.consumer.eformidling.EformidlingConstants.NAV_ORGNUMMER;
import static no.nav.dokdisteformidling.consumer.eformidling.EformidlingConstants.TRYGDERETTEN_ORGNUMMER;
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

@Component
public class AvtaltmeldingMapper {

    public static final String NAV_KLAGEINSTANS = "NAV Klageinstans";
    public static final String TRYGDERETTEN = "TRYGDERETTEN";
    public static final String SAKSPART_ROLLE_DAP = "DAP";
    public static final String SAKSPART_ROLLE_AMP = "AMP";
    public static final String INNGAAENDE = "I";
    public static final String UTGAAENDE = "U";
    public static final String FERDIGSTILT = "FERDIGSTILT";
    public static final String FILFORMAT_PNG = "PNG";
    public static final String FILFORMAT_JPEG = "JPEG";
    public static final String UKJENT = "UKJENT";

    private final PdlGraphQLConsumer pdlGraphQLConsumer;
    private final Ereg ereg;
    private final SafJournalpostQueryService<LightweightSafJournalpostQdist013> safJournalpostQueryService;

    public AvtaltmeldingMapper(@Qualifier("LightweightSafJournalpostQueryServiceQdist013") SafJournalpostQueryService<LightweightSafJournalpostQdist013> safJournalpostQueryService,
                               Ereg ereg, PdlGraphQLConsumer pdlGraphQLConsumer) {
        this.safJournalpostQueryService = safJournalpostQueryService;
        this.ereg = ereg;
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
        Saksmappe saksmappe = objectFactory.createSaksmappe();
        saksmappe.setTittel(journalpostQdist013.getTemanavn());
        saksmappe.setOpprettetDato(convertLocalDateTimeToXmlGregorianCalendar(journalpostQdist013.getSak().getDatoOpprettet()));
        saksmappe.setOpprettetAv(journalpostQdist013.getOpprettetAvNavn());
        saksmappe.setVirksomhetsspesifikkeMetadata(journalpostQdist013.getSak().getArkivsaksnummer());
        saksmappe.getPart()
                .add(createAndPopulatePartAMP(journalpostQdist013, objectFactory));
        saksmappe.getPart()
                .add(createAndPopulatePartDAP(journalpostQdist013, objectFactory));
        saksmappe.getRegistrering()
                .add(createAndPopulateJournalpost(journalpostQdist013, datoArkivmeldingOpprettet, objectFactory));
        saksmappe.setSaksdato(convertLocalDateTimeToXmlGregorianCalendar(journalpostQdist013.getSak().getDatoOpprettet()));
        saksmappe.setAdministrativEnhet(NAV_KLAGEINSTANS);
        saksmappe.setSaksansvarlig(journalpostQdist013.getOpprettetAvNavn());
        saksmappe.setJournalenhet(journalpostQdist013.getJournalfoerendeEnhet());
        saksmappe.setSaksstatus(UNDER_BEHANDLING);

        return saksmappe;
    }

    private Journalpost createAndPopulateJournalpost(JournalpostQdist013 journalpostQdist013, XMLGregorianCalendar datoArkivmeldingOpprettet, ObjectFactory objectFactory) {
        Journalpost journalpost = objectFactory.createJournalpost();
        journalpost.setOpprettetDato(convertLocalDateTimeToXmlGregorianCalendar(journalpostQdist013.getDatoOpprettet()));
        journalpost.setOpprettetAv(journalpostQdist013.getOpprettetAvNavn());
        addDokumentBeskrivelserToJournalpost(journalpost, journalpostQdist013, datoArkivmeldingOpprettet, objectFactory);
        journalpost.setTittel(journalpostQdist013.getTittel());
        journalpost.getKorrespondansepart().add(createAndPolpulateKorrespondanspartMottaker(objectFactory));
        journalpost.getKorrespondansepart().add(createAndPolpulateKorrespondanspartAvsender(objectFactory));
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
        if (!isHoveddok && !isBlank(dokumentInfo.getOriginalJournalpostId())) {
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
        if (!isHoveddok && !isBlank(dokumentInfo.getOriginalJournalpostId()) && !isJournalDatoNull(dokumentInfo.getOriginalJournalpostId())) {
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
        if (!isHoveddok && !isBlank(dokumentInfo.getOriginalJournalpostId())) {
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
        dokumentobjekt.setVariantformat(getDokumentVariant(dokumentInfo));
        dokumentobjekt.setOpprettetDato(getDokumentDatoJournalfoert(isHoveddokument, journalpostQdist013, dokumentInfo));
        dokumentobjekt.setOpprettetAv(getDokumentJournalfortAvNavn(isHoveddokument, journalpostQdist013, dokumentInfo));
        dokumentobjekt.setReferanseDokumentfil(getReferanseDokumentFil(journalpostQdist013.getJournalpostId(), dokumentInfo));

        return dokumentobjekt;
    }

    private String getReferanseDokumentFil(String journalpostId, JournalpostQdist013.DokumentInfo dokumentInfo) {
        return format("%s-%s-%s-%s", journalpostId, dokumentInfo.getDokumentInfoId(), getDokumentVariant(dokumentInfo), getFiltype(dokumentInfo));
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
        korrespondansepartAvsender.setKorrespondanseparttype(AVSENDER);
        korrespondansepartAvsender.setKorrespondansepartNavn(NAV_KLAGEINSTANS);
        korrespondansepartAvsender.setOrganisasjonsnummer(EnhetsidentifikatorType.builder()
                .withOrganisasjonsnummer(TRYGDERETTEN_ORGNUMMER)
                .build());
        return korrespondansepartAvsender;
    }

    private Korrespondansepart createAndPolpulateKorrespondanspartMottaker(ObjectFactory objectFactory) {
        Korrespondansepart korrespondansepartMottaker = objectFactory.createKorrespondansepart();
        korrespondansepartMottaker.setKorrespondanseparttype(MOTTAKER);
        korrespondansepartMottaker.setKorrespondansepartNavn(TRYGDERETTEN);
        korrespondansepartMottaker.setOrganisasjonsnummer(EnhetsidentifikatorType.builder()
                .withOrganisasjonsnummer(TRYGDERETTEN_ORGNUMMER)
                .build());
        return korrespondansepartMottaker;
    }

    private Part createAndPopulatePartDAP(JournalpostQdist013 journalpostQdist013, ObjectFactory objectFactory) {
        Part partDAP = objectFactory.createPart();
        partDAP.setPartNavn(getSakspartNavnDAP(journalpostQdist013));
        partDAP.setPartRolle(SAKSPART_ROLLE_DAP);
        partDAP.setOrganisasjonsnummer(EnhetsidentifikatorType.builder()
                .withOrganisasjonsnummer(hentOrgNummerDAP(journalpostQdist013))
                .build());
        partDAP.setFoedselsnummer(FoedselsnummerType.builder()
                .withFoedselsnummer(getFoedselsnummer(journalpostQdist013))
                .build());
        return partDAP;
    }

    private Part createAndPopulatePartAMP(JournalpostQdist013 journalpostQdist013, ObjectFactory objectFactory) {
        Part partAMP = objectFactory.createPart();
        partAMP.setPartNavn(NAV_KLAGEINSTANS);
        partAMP.setPartRolle(SAKSPART_ROLLE_AMP);
        partAMP.setOrganisasjonsnummer(EnhetsidentifikatorType.builder()
                .withOrganisasjonsnummer(NAV_ORGNUMMER)
                .build());
        partAMP.setKontaktperson(journalpostQdist013.getOpprettetAvNavn());
        return partAMP;
    }

    private String getFoedselsnummer(JournalpostQdist013 journalpostQdist013) {
        if (brukerTypeIsAktoerId(journalpostQdist013)) {
            return pdlGraphQLConsumer.hentNavn(journalpostQdist013.getBruker().getId(), journalpostQdist013.getTema()).getIdent();
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
        if (brukerTypeIsOrgnr(journalpostQdist013)) {
            return ereg.hentNavn(journalpostQdist013.getBruker().getId());
        } else {
            return getHentPersonInfo(journalpostQdist013.getBruker().getId(), journalpostQdist013.getTema()).getFulltnavn();
        }
    }

    private HentPersonInfo getHentPersonInfo(String ident, String tema) {
        return pdlGraphQLConsumer.hentNavn(ident, tema);
    }
}
