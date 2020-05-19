package no.nav.dokdisteformidling.qdist013;

import no.arkivverket.standarder.noark5.arkivmelding.Arkivmelding;
import no.arkivverket.standarder.noark5.arkivmelding.Dokumentbeskrivelse;
import no.arkivverket.standarder.noark5.arkivmelding.Dokumentobjekt;
import no.arkivverket.standarder.noark5.arkivmelding.Journalpost;
import no.arkivverket.standarder.noark5.arkivmelding.Korrespondansepart;
import no.arkivverket.standarder.noark5.arkivmelding.Mappe;
import no.arkivverket.standarder.noark5.arkivmelding.Part;
import no.arkivverket.standarder.noark5.arkivmelding.Registrering;
import no.arkivverket.standarder.noark5.arkivmelding.Saksmappe;
import no.nav.dokdisteformidling.consumer.aktoerregister.Aktoerregister;
import no.nav.dokdisteformidling.consumer.ereg.Ereg;
import no.nav.dokdisteformidling.consumer.saf.SafJournalpostQueryService;
import no.nav.dokdisteformidling.consumer.tps.Tps;
import no.nav.dokdisteformidling.qdist013.saf.lightweight.LightweightSafJournalpostQdist013;
import no.nav.dokdisteformidling.qdist013.saf.main.JournalpostQdist013;
import no.nav.dokdisteformidling.qdist013.util.AvtaltmeldingConstant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.xml.bind.JAXBElement;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static no.nav.dokdisteformidling.constants.DomainConstants.APP_NAME;
import static no.nav.dokdisteformidling.constants.DomainConstants.VARIANTFORMAT_ARKIV;
import static no.nav.dokdisteformidling.constants.DomainConstants.VARIANTFORMAT_PRODUKSJON;
import static no.nav.dokdisteformidling.constants.DomainConstants.VARIANTFORMAT_SLADDET;
import static no.nav.dokdisteformidling.qdist013.AvtaltmeldingMapper.FERDIGSTILT;
import static no.nav.dokdisteformidling.qdist013.AvtaltmeldingMapper.INNGAAENDE;
import static no.nav.dokdisteformidling.qdist013.AvtaltmeldingMapper.NAV_KLAGEINSTANS;
import static no.nav.dokdisteformidling.qdist013.AvtaltmeldingMapper.SAKSPART_ROLLE_AMP;
import static no.nav.dokdisteformidling.qdist013.AvtaltmeldingMapper.SAKSPART_ROLLE_DAP;
import static no.nav.dokdisteformidling.qdist013.AvtaltmeldingMapper.TRYGDERETTEN;
import static no.nav.dokdisteformidling.qdist013.AvtaltmeldingMapper.UKJENT;
import static no.nav.dokdisteformidling.qdist013.AvtaltmeldingMapper.UTGAAENDE;
import static no.nav.dokdisteformidling.qdist013.TestUtil.convertFromXmlGregorianCalendarToLocalDateTime;
import static no.nav.dokdisteformidling.qdist013.util.AvtaltmeldingConstant.ARKIVFORMAT;
import static no.nav.dokdisteformidling.qdist013.util.AvtaltmeldingConstant.AVSENDER;
import static no.nav.dokdisteformidling.qdist013.util.AvtaltmeldingConstant.DOKUMENTASJON;
import static no.nav.dokdisteformidling.qdist013.util.AvtaltmeldingConstant.DOKUMENTET_ER_FERDIGSTILT;
import static no.nav.dokdisteformidling.qdist013.util.AvtaltmeldingConstant.DOKUMENT_HVOR_DELER_AV_INNHOLDET_ER_SKJERMET;
import static no.nav.dokdisteformidling.qdist013.util.AvtaltmeldingConstant.EKSPEDERT;
import static no.nav.dokdisteformidling.qdist013.util.AvtaltmeldingConstant.MOTTAKER;
import static no.nav.dokdisteformidling.qdist013.util.AvtaltmeldingConstant.PRODUKSJONSFORMAT;
import static no.nav.dokdisteformidling.qdist013.util.AvtaltmeldingConstant.UNDER_BEHANDLING;
import static no.nav.dokdisteformidling.qdist013.util.AvtaltmeldingConstant.UTGAAENDE_DOKUMENT;
import static no.nav.dokdisteformidling.qdist013.util.AvtaltmeldingConstant.VEDLEGG;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
class AvtaltmeldingMapperTest {

    private static final String BESTILLINGS_ID = "bestillingsId";
    private static final String JOURNALPOST_ID = "987654321";

    private static final String ARKIV_SAKNUMMER = "111111";
    private static final LocalDateTime DATO_OPPRETTET_SAK = LocalDateTime.now();
    private static final LocalDateTime DATO_OPPRETTET_JOURNALPOST = LocalDateTime.now().minusDays(1);
    private static final LocalDateTime DATO_JOURNALFOERT = LocalDateTime.now().minusDays(2);
    private static final String OPPRETTET_AV_NAVN = "opprettetAvNavn";
    private static final String BRUKER_ID_FNR = "brukerIdFnr";
    private static final String BRUKER_TYPE_FNR = "FNR";
    private static final String BRUKER_ID_ORGNR = "brukerIdOrgNr";
    private static final String BRUKER_TYPE_ORGNR = "ORGNR";
    private static final String BRUKER_ID_AKTOER_ID = "aktoerId";
    private static final String BRUKER_TYPE_AKTOER_ID = "AKTOERID";
    private static final String TITTEL = "tittel";
    private static final String JOURNALFOERT_AV_NAVN = "journalfoertAvNavn";
    private static final String TEMA_NAVN = "temaNavn";
    private static final String OPPRETTET_AV_UKJENT = "UKJENT";

    private static final String DOKUMENT_INFO_ID_HOVEDDOK = "1234567";
    private static final String TITTEL_HOVEDDOK = "tittelHoveddok";

    private static final String DOKUMENT_INFO_ID_VEDLEGG = "7654321";
    private static final String TITTEL_VEDLEGG = "tittelVedlegg";
    private static final String ORIGINAL_JPID_VEDLEGG = "1111111111";

    private static final String DOKUMENT_INFO_ID_VEDLEGG_2 = "9876543";

    private static final String FNR_FOR_AKTOER_ID = "222222222";
    private static final String EREG_NAVN = "ereg_navn";
    private static final String TPS_NAVN = "tps_navn";

    private static final String AVSENDER_MOTTAKER_NAVN_ORIG_JP = "avsenderMottakerNavnOrigJp";
    private static final String JOURNALFOERT_AV_NAVN_ORIG_JP = "ajournalfoertAvNavnOrigJp";
    private static final LocalDateTime DATO_JOURNALFOERT_ORIG_JP = LocalDateTime.now().minusDays(5);

    private static final String FILTYPE_PNG = "PNG";
    private static final String FILTYPE_JPEG = "JPEG";
    private static final String FILTYPE_PDF = "PDF";
    private static final String FILTYPE_PDFA = "PDF/A";


    private Aktoerregister aktoerregisterMock;
    private Ereg eregMock;
    private Tps tpsMock;
    private SafJournalpostQueryService safJournalpostQueryServiceMock;
    private AvtaltmeldingMapper avtaltmeldingMapper;

    @BeforeEach
    public void setUp() {
        aktoerregisterMock = mock(Aktoerregister.class);
        eregMock = mock(Ereg.class);
        tpsMock = mock(Tps.class);
        safJournalpostQueryServiceMock = mock(SafJournalpostQueryService.class);
        avtaltmeldingMapper = new AvtaltmeldingMapper(safJournalpostQueryServiceMock, aktoerregisterMock, eregMock, tpsMock);
    }

    @Test
    @DisplayName("Asserts all fields")
    void fullHappyPath() {
        when(tpsMock.hentNavn(any(String.class))).thenReturn(TPS_NAVN);
        JAXBElement<Arkivmelding> arkivmeldingJAXBElement = avtaltmeldingMapper.createArkivMelding(createJournalpostQdist013Builder()
                .build(), BESTILLINGS_ID);

        assertThat(arkivmeldingJAXBElement, notNullValue());
        Arkivmelding arkivmelding = arkivmeldingJAXBElement.getValue();
        assertArkivmelding(arkivmelding);

        verify(tpsMock, times(1)).hentNavn(BRUKER_ID_FNR);
        verify(eregMock, times(0)).hentNavn(any(String.class));

    }

    @Test
    @DisplayName("Case when bruker is organisasjon. Should get name from Ereg")
    void happyPathBrukerIsOrgansisasjon() {
        when(eregMock.hentNavn(any(String.class))).thenReturn(EREG_NAVN);

        JournalpostQdist013 journalpostQdist013 = createJournalpostQdist013Builder()
                .bruker(JournalpostQdist013.Bruker.builder()
                        .id(BRUKER_ID_ORGNR)
                        .type(BRUKER_TYPE_ORGNR)
                        .build())
                .build();

        JAXBElement<Arkivmelding> arkivmeldingJAXBElement = avtaltmeldingMapper.createArkivMelding(journalpostQdist013, BESTILLINGS_ID);
        Arkivmelding arkivmelding = arkivmeldingJAXBElement.getValue();
        List<Mappe> mappeList = arkivmelding.getMappe();
        Saksmappe saksmappe = (Saksmappe) mappeList.get(0);
        Part sakspartDAP = saksmappe.getPart().get(1);

        assertEquals(sakspartDAP.getPartNavn(), EREG_NAVN);
        assertEquals(sakspartDAP.getPartRolle(), SAKSPART_ROLLE_DAP);
        assertNull(sakspartDAP.getKontaktperson());

        verify(eregMock, times(1)).hentNavn(BRUKER_ID_ORGNR);
        verify(tpsMock, times(0)).hentNavn(any(String.class));
        verify(safJournalpostQueryServiceMock, times(0)).hentJournalpost(any(String.class));

    }

    @Test
    @DisplayName("Case when bruker is aktoer. Should get fnr from aktoerregister")
    void happyPathBrukerIsAktoer() {
        when(aktoerregisterMock.hentIdentForAktoerId(any(String.class))).thenReturn(FNR_FOR_AKTOER_ID);
        when(tpsMock.hentNavn(any(String.class))).thenReturn(TPS_NAVN);

        JournalpostQdist013 journalpostQdist013 = createJournalpostQdist013Builder()
                .bruker(JournalpostQdist013.Bruker.builder()
                        .id(BRUKER_ID_AKTOER_ID)
                        .type(BRUKER_TYPE_AKTOER_ID)
                        .build())
                .build();

        JAXBElement<Arkivmelding> arkivmeldingJAXBElement = avtaltmeldingMapper.createArkivMelding(journalpostQdist013, BESTILLINGS_ID);
        Arkivmelding arkivmelding = arkivmeldingJAXBElement.getValue();
        List<Mappe> mappeList = arkivmelding.getMappe();
        Saksmappe saksmappe = (Saksmappe) mappeList.get(0);
        Part sakspartDAP = saksmappe.getPart().get(1);

        assertEquals(sakspartDAP.getPartNavn(), TPS_NAVN);
        assertEquals(sakspartDAP.getPartRolle(), SAKSPART_ROLLE_DAP);
        assertNull(sakspartDAP.getKontaktperson());

        verify(aktoerregisterMock, times(2)).hentIdentForAktoerId(BRUKER_ID_AKTOER_ID);
        verify(tpsMock, times(1)).hentNavn(FNR_FOR_AKTOER_ID);
        verify(eregMock, times(0)).hentNavn(any(String.class));
        verify(safJournalpostQueryServiceMock, times(0)).hentJournalpost(any(String.class));
    }

    @Test
    @DisplayName("Case for satt originalJournalPostId men ukjent datoJournal")
    void shouldMapOpprettetDatoWhenNullDatoJournalISafJournalpostgetJournalfortAndVedleggHasOriginalJpId() {
        JournalpostQdist013 journalpostQdist013 = createJournalpostQdist013Builder()
                .journalposttype(INNGAAENDE)
                .dokumenter(Arrays.asList(createHoveddokumentBuilder().build(),
                        createVedleggBuilderUtenDato().build()))
                .build();

        JAXBElement<Arkivmelding> arkivmeldingJAXBElement = avtaltmeldingMapper.createArkivMelding(journalpostQdist013, BESTILLINGS_ID);
        Arkivmelding arkivmelding = arkivmeldingJAXBElement.getValue();
        List<Mappe> mappeList = arkivmelding.getMappe();
        Saksmappe saksmappe = (Saksmappe) mappeList.get(0);
        Journalpost registreringJp = (Journalpost) saksmappe.getRegistrering().get(0);
        Dokumentbeskrivelse dokumentbeskrivelse = (Dokumentbeskrivelse) registreringJp.getDokumentbeskrivelse()
                .get(1);

        assertEquals(dokumentbeskrivelse.getTittel(), TITTEL_VEDLEGG );

        verify(safJournalpostQueryServiceMock, times(0)).hentJournalpost(ORIGINAL_JPID_VEDLEGG);
    }

    @Test
    @DisplayName("Case when journalposttype is inngaaende and vedlegg has original jpId. Should make a custom dokumenttittel")
    void happyPathTestTittelWhenJpIsInngaaendeAndVedleggHasOriginalJpId() {
        when(safJournalpostQueryServiceMock.hentJournalpost(ORIGINAL_JPID_VEDLEGG)).thenReturn(createLightweightSafJournalpostQdist013());

        JournalpostQdist013 journalpostQdist013 = createJournalpostQdist013Builder()
                .journalposttype(INNGAAENDE)
                .dokumenter(Arrays.asList(createHoveddokumentBuilder().build(),
                        createVedleggBuilder().originalJournalpostId(ORIGINAL_JPID_VEDLEGG).build()))
                .build();

        JAXBElement<Arkivmelding> arkivmeldingJAXBElement = avtaltmeldingMapper.createArkivMelding(journalpostQdist013, BESTILLINGS_ID);
        Arkivmelding arkivmelding = arkivmeldingJAXBElement.getValue();
        List<Mappe> mappeList = arkivmelding.getMappe();
        Saksmappe saksmappe = (Saksmappe) mappeList.get(0);
        Journalpost registreringJp = (Journalpost) saksmappe.getRegistrering().get(0);
        Dokumentbeskrivelse dokumentbeskrivelse = (Dokumentbeskrivelse) registreringJp.getDokumentbeskrivelse()
                .get(1);

        assertEquals(dokumentbeskrivelse.getTittel(), TITTEL_VEDLEGG + ", Fra " + AVSENDER_MOTTAKER_NAVN_ORIG_JP);

        verify(safJournalpostQueryServiceMock, times(10)).hentJournalpost(ORIGINAL_JPID_VEDLEGG);
    }

    @Test
    @DisplayName("Case when journalposttype is utgaaende and vedlegg has original jpId. Should make a custom dokumenttittel")
    void happyPathTestTittelWhenJpIsUtgaaendeAndVedleggHasOriginalJpId() {
        when(safJournalpostQueryServiceMock.hentJournalpost(ORIGINAL_JPID_VEDLEGG)).thenReturn(createLightweightSafJournalpostQdist013());

        JournalpostQdist013 journalpostQdist013 = createJournalpostQdist013Builder()
                .journalposttype(UTGAAENDE)
                .dokumenter(Arrays.asList(createHoveddokumentBuilder().build(),
                        createVedleggBuilder().originalJournalpostId(ORIGINAL_JPID_VEDLEGG).build()))
                .build();

        JAXBElement<Arkivmelding> arkivmeldingJAXBElement = avtaltmeldingMapper.createArkivMelding(journalpostQdist013, BESTILLINGS_ID);
        Arkivmelding arkivmelding = arkivmeldingJAXBElement.getValue();
        List<Mappe> mappeList = arkivmelding.getMappe();
        Saksmappe saksmappe = (Saksmappe) mappeList.get(0);
        Journalpost registreringJp = (Journalpost) saksmappe.getRegistrering().get(0);
        Dokumentbeskrivelse dokumentbeskrivelse = (Dokumentbeskrivelse) registreringJp.getDokumentbeskrivelse()
                .get(1);

        assertEquals(dokumentbeskrivelse.getTittel(), TITTEL_VEDLEGG + ", Til " + AVSENDER_MOTTAKER_NAVN_ORIG_JP);

        verify(safJournalpostQueryServiceMock, times(10)).hentJournalpost(ORIGINAL_JPID_VEDLEGG);
    }

    @Test
    @DisplayName("Case when journalposttype is notat and vedlegg has original jpId. Should not make a custom dokumenttittel")
    void happyPathTestTittelWhenJpIsNotatAndVedleggHasOriginalJpId() {
        when(safJournalpostQueryServiceMock.hentJournalpost(ORIGINAL_JPID_VEDLEGG)).thenReturn(createLightweightSafJournalpostQdist013());

        JournalpostQdist013 journalpostQdist013 = createJournalpostQdist013Builder()
                .journalposttype("Notat")
                .dokumenter(Arrays.asList(createHoveddokumentBuilder().build(),
                        createVedleggBuilder().originalJournalpostId(ORIGINAL_JPID_VEDLEGG).build()))
                .build();

        JAXBElement<Arkivmelding> arkivmeldingJAXBElement = avtaltmeldingMapper.createArkivMelding(journalpostQdist013, BESTILLINGS_ID);
        Arkivmelding arkivmelding = arkivmeldingJAXBElement.getValue();
        List<Mappe> mappeList = arkivmelding.getMappe();
        Saksmappe saksmappe = (Saksmappe) mappeList.get(0);
        Journalpost registreringJp = (Journalpost) saksmappe.getRegistrering().get(0);
        Dokumentbeskrivelse dokumentbeskrivelse = (Dokumentbeskrivelse) registreringJp.getDokumentbeskrivelse()
                .get(1);

        assertEquals(dokumentbeskrivelse.getTittel(), TITTEL_VEDLEGG);

        verify(safJournalpostQueryServiceMock, times(8)).hentJournalpost(ORIGINAL_JPID_VEDLEGG);
    }

    @Test
    @DisplayName("Case when vedlegg has original jpId. Should get opprettet dato from original journalpost")
    void happyPathTestOpprettetDatoWhenVedleggHasOriginalJpId() {
        when(safJournalpostQueryServiceMock.hentJournalpost(ORIGINAL_JPID_VEDLEGG)).thenReturn(createLightweightSafJournalpostQdist013());

        JournalpostQdist013 journalpostQdist013 = createJournalpostQdist013Builder()
                .dokumenter(Arrays.asList(createHoveddokumentBuilder().build(),
                        createVedleggBuilder().originalJournalpostId(ORIGINAL_JPID_VEDLEGG).build()))
                .build();

        JAXBElement<Arkivmelding> arkivmeldingJAXBElement = avtaltmeldingMapper.createArkivMelding(journalpostQdist013, BESTILLINGS_ID);
        Arkivmelding arkivmelding = arkivmeldingJAXBElement.getValue();
        List<Mappe> mappeList = arkivmelding.getMappe();
        Saksmappe saksmappe = (Saksmappe) mappeList.get(0);
        Journalpost registreringJp = (Journalpost) saksmappe.getRegistrering().get(0);

        Dokumentbeskrivelse dokumentbeskrivelseHoveddok = (Dokumentbeskrivelse) registreringJp.getDokumentbeskrivelse()
                .get(0);
        Dokumentobjekt dokumentobjektHoveddok = dokumentbeskrivelseHoveddok.getDokumentobjekt().get(0);
        Dokumentbeskrivelse dokumentbeskrivelseVedlegg = (Dokumentbeskrivelse) registreringJp.getDokumentbeskrivelse()
                .get(1);
        Dokumentobjekt dokumentobjektVedlegg = dokumentbeskrivelseVedlegg.getDokumentobjekt().get(0);

        assertEquals(convertFromXmlGregorianCalendarToLocalDateTime(dokumentbeskrivelseHoveddok.getOpprettetDato()), DATO_JOURNALFOERT);
        assertEquals(convertFromXmlGregorianCalendarToLocalDateTime(dokumentobjektHoveddok.getOpprettetDato()), DATO_JOURNALFOERT);
        assertEquals(convertFromXmlGregorianCalendarToLocalDateTime(dokumentbeskrivelseVedlegg.getOpprettetDato()), DATO_JOURNALFOERT_ORIG_JP);
        assertEquals(convertFromXmlGregorianCalendarToLocalDateTime(dokumentobjektVedlegg.getOpprettetDato()), DATO_JOURNALFOERT_ORIG_JP);

        verify(safJournalpostQueryServiceMock, times(10)).hentJournalpost(ORIGINAL_JPID_VEDLEGG);
    }

    @Test
    @DisplayName("Case when vedlegg has original jpId. Should get opprettet av from original journalpost")
    void happyPathTestOpprettetAvWhenVedleggHasOriginalJpId() {
        when(safJournalpostQueryServiceMock.hentJournalpost(ORIGINAL_JPID_VEDLEGG)).thenReturn(createLightweightSafJournalpostQdist013());

        JournalpostQdist013 journalpostQdist013 = createJournalpostQdist013Builder()
                .dokumenter(Arrays.asList(createHoveddokumentBuilder().build(),
                        createVedleggBuilder().originalJournalpostId(ORIGINAL_JPID_VEDLEGG).build()))
                .build();

        JAXBElement<Arkivmelding> arkivmeldingJAXBElement = avtaltmeldingMapper.createArkivMelding(journalpostQdist013, BESTILLINGS_ID);
        Arkivmelding arkivmelding = arkivmeldingJAXBElement.getValue();
        List<Mappe> mappeList = arkivmelding.getMappe();
        Saksmappe saksmappe = (Saksmappe) mappeList.get(0);
        Journalpost registreringJp = (Journalpost) saksmappe.getRegistrering().get(0);

        Dokumentbeskrivelse dokumentbeskrivelseHoveddok = (Dokumentbeskrivelse) registreringJp.getDokumentbeskrivelse()
                .get(0);
        Dokumentobjekt dokumentobjektHoveddok = dokumentbeskrivelseHoveddok.getDokumentobjekt().get(0);
        Dokumentbeskrivelse dokumentbeskrivelseVedlegg = (Dokumentbeskrivelse) registreringJp.getDokumentbeskrivelse()
                .get(1);
        Dokumentobjekt dokumentobjektVedlegg = dokumentbeskrivelseVedlegg.getDokumentobjekt().get(0);

        assertEquals(dokumentbeskrivelseHoveddok.getOpprettetAv(), JOURNALFOERT_AV_NAVN);
        assertEquals(dokumentobjektHoveddok.getOpprettetAv(), JOURNALFOERT_AV_NAVN);
        assertEquals(dokumentbeskrivelseVedlegg.getOpprettetAv(), JOURNALFOERT_AV_NAVN_ORIG_JP);
        assertEquals(dokumentobjektVedlegg.getOpprettetAv(), JOURNALFOERT_AV_NAVN_ORIG_JP);

        verify(safJournalpostQueryServiceMock, times(10)).hentJournalpost(ORIGINAL_JPID_VEDLEGG);
    }

    @Test
    @DisplayName("Case when dokument does not have variantformat SLADDET (variantformat is ARKIV) and filtype is not PNG or JPEG. Should set variantformat to Produksjonsformat")
    void happyPathTestNoSladdetVariantAndFiltypeNotPngOrJPEG() {
        when(safJournalpostQueryServiceMock.hentJournalpost(ORIGINAL_JPID_VEDLEGG)).thenReturn(createLightweightSafJournalpostQdist013());

        JournalpostQdist013 journalpostQdist013 = createJournalpostQdist013Builder()
                .dokumenter(Arrays.asList(createHoveddokumentBuilder()
                        .dokumentvarianter(Arrays.asList(JournalpostQdist013.DokumentInfo.Dokumentvariant.builder()
                                        .variantformat(VARIANTFORMAT_ARKIV)
                                        .filtype(FILTYPE_PDFA)
                                        .build(),
                                JournalpostQdist013.DokumentInfo.Dokumentvariant.builder()
                                        .variantformat(VARIANTFORMAT_PRODUKSJON)
                                        .filtype(FILTYPE_PDF)
                                        .build()))
                        .build()))
                .build();

        JAXBElement<Arkivmelding> arkivmeldingJAXBElement = avtaltmeldingMapper.createArkivMelding(journalpostQdist013, BESTILLINGS_ID);
        Arkivmelding arkivmelding = arkivmeldingJAXBElement.getValue();
        List<Mappe> mappeList = arkivmelding.getMappe();
        Saksmappe saksmappe = (Saksmappe) mappeList.get(0);
        Journalpost registreringJp = (Journalpost) saksmappe.getRegistrering().get(0);

        Dokumentbeskrivelse dokumentbeskrivelseHoveddok = (Dokumentbeskrivelse) registreringJp.getDokumentbeskrivelse()
                .get(0);
        Dokumentobjekt dokumentobjektHoveddok = dokumentbeskrivelseHoveddok.getDokumentobjekt().get(0);

        assertEquals(dokumentobjektHoveddok.getVariantformat(), PRODUKSJONSFORMAT);
        assertTrue(dokumentobjektHoveddok.getReferanseDokumentfil().contains(PRODUKSJONSFORMAT));
    }

    @Test
    @DisplayName("Case when dokument does not have variantformat SLADDET (variantformat is ARKIV) and filtype is PNG. Should set variantformat to Arkivformat")
    void happyPathTestNoSladdetVariantAndFiltypeIsPng() {
        when(safJournalpostQueryServiceMock.hentJournalpost(ORIGINAL_JPID_VEDLEGG)).thenReturn(createLightweightSafJournalpostQdist013());

        JournalpostQdist013 journalpostQdist013 = createJournalpostQdist013Builder()
                .dokumenter(Arrays.asList(createHoveddokumentBuilder()
                        .dokumentvarianter(Arrays.asList(JournalpostQdist013.DokumentInfo.Dokumentvariant.builder()
                                        .variantformat(VARIANTFORMAT_ARKIV)
                                        .filtype(FILTYPE_PNG)
                                        .build(),
                                JournalpostQdist013.DokumentInfo.Dokumentvariant.builder()
                                        .variantformat(VARIANTFORMAT_PRODUKSJON)
                                        .filtype(FILTYPE_PDF)
                                        .build()))
                        .build()))
                .build();

        JAXBElement<Arkivmelding> arkivmeldingJAXBElement = avtaltmeldingMapper.createArkivMelding(journalpostQdist013, BESTILLINGS_ID);
        Arkivmelding arkivmelding = arkivmeldingJAXBElement.getValue();
        List<Mappe> mappeList = arkivmelding.getMappe();
        Saksmappe saksmappe = (Saksmappe) mappeList.get(0);
        Journalpost registreringJp = (Journalpost) saksmappe.getRegistrering().get(0);

        Dokumentbeskrivelse dokumentbeskrivelseHoveddok = (Dokumentbeskrivelse) registreringJp.getDokumentbeskrivelse()
                .get(0);
        Dokumentobjekt dokumentobjektHoveddok = dokumentbeskrivelseHoveddok.getDokumentobjekt().get(0);

        assertEquals(dokumentobjektHoveddok.getVariantformat(), ARKIVFORMAT);
        assertTrue(dokumentobjektHoveddok.getReferanseDokumentfil().contains(ARKIVFORMAT));
    }

    @Test
    @DisplayName("Case when dokument does not have variantformat SLADDET (variantformat is ARKIV) and filtype is JPEG. Should set variantformat to Arkivformat")
    void happyPathTestNoSladdetVariantAndFiltypeIsJPEG() {
        when(safJournalpostQueryServiceMock.hentJournalpost(ORIGINAL_JPID_VEDLEGG)).thenReturn(createLightweightSafJournalpostQdist013());

        JournalpostQdist013 journalpostQdist013 = createJournalpostQdist013Builder()
                .dokumenter(Arrays.asList(createHoveddokumentBuilder()
                        .dokumentvarianter(Arrays.asList(JournalpostQdist013.DokumentInfo.Dokumentvariant.builder()
                                        .variantformat(VARIANTFORMAT_ARKIV)
                                        .filtype(FILTYPE_JPEG)
                                        .build(),
                                JournalpostQdist013.DokumentInfo.Dokumentvariant.builder()
                                        .variantformat(VARIANTFORMAT_PRODUKSJON)
                                        .filtype(FILTYPE_PDF)
                                        .build()))
                        .build()))
                .build();

        JAXBElement<Arkivmelding> arkivmeldingJAXBElement = avtaltmeldingMapper.createArkivMelding(journalpostQdist013, BESTILLINGS_ID);
        Arkivmelding arkivmelding = arkivmeldingJAXBElement.getValue();
        List<Mappe> mappeList = arkivmelding.getMappe();
        Saksmappe saksmappe = (Saksmappe) mappeList.get(0);
        Journalpost registreringJp = (Journalpost) saksmappe.getRegistrering().get(0);

        Dokumentbeskrivelse dokumentbeskrivelseHoveddok = (Dokumentbeskrivelse) registreringJp.getDokumentbeskrivelse()
                .get(0);
        Dokumentobjekt dokumentobjektHoveddok = dokumentbeskrivelseHoveddok.getDokumentobjekt().get(0);

        assertEquals(dokumentobjektHoveddok.getVariantformat(), ARKIVFORMAT);
        assertTrue(dokumentobjektHoveddok.getReferanseDokumentfil().contains(ARKIVFORMAT));
    }

    @Test
    @DisplayName("Case when vedlegg has no dokumentstatus set. That vedlegg should be considered FERDIGSTILT.")
    void happyPathVedleggFerdigstiltUtenStatus() {
        JournalpostQdist013 journalpostQdist013 = createJournalpostQdist013Builder()
                .dokumenter(Arrays.asList(createHoveddokumentBuilder().build(),
                        createVedleggBuilder().dokumentstatus(null).build()))
                .build();

        JAXBElement<Arkivmelding> arkivmeldingJAXBElement = avtaltmeldingMapper.createArkivMelding(journalpostQdist013, BESTILLINGS_ID);
        Arkivmelding arkivmelding = arkivmeldingJAXBElement.getValue();
        List<Mappe> mappeList = arkivmelding.getMappe();
        Saksmappe saksmappe = (Saksmappe) mappeList.get(0);
        Journalpost registreringJp = (Journalpost) saksmappe.getRegistrering().get(0);

        assertEquals(2, arkivmelding.getAntallFiler());
        assertDokumentbeskrivelseOpprettetAv(registreringJp.getDokumentbeskrivelse());
    }

    @Test
    @DisplayName("Case when vedlegg does not have dokumentstatus FERDIGSTILT. That vedlegg should not be mapped.")
    void happyPathIkkeFerdigstiltVedlegg() {
        when(safJournalpostQueryServiceMock.hentJournalpost(ORIGINAL_JPID_VEDLEGG)).thenReturn(createLightweightSafJournalpostQdist013());
        JournalpostQdist013 journalpostQdist013 = createJournalpostQdist013Builder()
                .dokumenter(Arrays.asList(createHoveddokumentBuilder().build(),
                        createVedleggBuilder().dokumentInfoId(DOKUMENT_INFO_ID_VEDLEGG_2)
                                .dokumentstatus("UNDER_REDIGERING")
                                .build(),
                        createVedleggBuilder().build()))
                .build();

        JAXBElement<Arkivmelding> arkivmeldingJAXBElement = avtaltmeldingMapper.createArkivMelding(journalpostQdist013, BESTILLINGS_ID);
        Arkivmelding arkivmelding = arkivmeldingJAXBElement.getValue();
        List<Mappe> mappeList = arkivmelding.getMappe();
        Saksmappe saksmappe = (Saksmappe) mappeList.get(0);
        Journalpost registreringJp = (Journalpost) saksmappe.getRegistrering().get(0);

        assertEquals(2, arkivmelding.getAntallFiler());
        assertDokumentbeskrivelseOpprettetAv(registreringJp.getDokumentbeskrivelse());
    }

    @Test
    @DisplayName("Case for satt originalJournalPostId men ukjent journalfører")
    void assertUkjentVedsafJournalpostgetJournalfortAvNavnErNull() {
        when(safJournalpostQueryServiceMock.hentJournalpost(ORIGINAL_JPID_VEDLEGG)).thenReturn(createLightweightSafJournalpostQdist013NoJournalFoertAv());

        JournalpostQdist013 journalpostQdist013 = createJournalpostQdist013BuilderNoJournalFoertAv()
                .dokumenter(Arrays.asList(createHoveddokumentBuilder().build(),
                        createVedleggBuilder().originalJournalpostId(ORIGINAL_JPID_VEDLEGG).build()))
                .build();

        JAXBElement<Arkivmelding> arkivmeldingJAXBElement = avtaltmeldingMapper.createArkivMelding(journalpostQdist013, BESTILLINGS_ID);
        Arkivmelding arkivmelding = arkivmeldingJAXBElement.getValue();
        List<Mappe> mappeList = arkivmelding.getMappe();
        Saksmappe saksmappe = (Saksmappe) mappeList.get(0);
        Journalpost registreringJp = (Journalpost) saksmappe.getRegistrering().get(0);

        Dokumentbeskrivelse dokumentBeskrivelseVedlegg = (Dokumentbeskrivelse) registreringJp.getDokumentbeskrivelse()
                .get(1);
        Dokumentobjekt dokumentVedlegg = dokumentBeskrivelseVedlegg.getDokumentobjekt().get(0);

        assertEquals(dokumentVedlegg.getOpprettetAv(), UKJENT);
    }

    private void assertArkivmelding(Arkivmelding arkivmelding) {
        assertNotNull(arkivmelding);
        assertEquals(arkivmelding.getSystem(), APP_NAME);
        assertEquals(arkivmelding.getMeldingId(), BESTILLINGS_ID);
        assertNotNull(arkivmelding.getTidspunkt());
        assertEquals(arkivmelding.getAntallFiler(), 2);
        assertMappe(arkivmelding.getMappe());
    }

    private void assertMappe(List<Mappe> mappeList) {
        assertTrue(mappeList != null && mappeList.size() == 1);
        assertTrue(mappeList.get(0) instanceof Saksmappe);
        Saksmappe saksmappe = (Saksmappe) mappeList.get(0);

        assertEquals(saksmappe.getTittel(), TEMA_NAVN);
        assertEquals(convertFromXmlGregorianCalendarToLocalDateTime(saksmappe.getOpprettetDato()), DATO_OPPRETTET_SAK);
        assertEquals(saksmappe.getOpprettetAv(), OPPRETTET_AV_NAVN);
        assertRegistrering(saksmappe.getRegistrering());
        assertEquals(convertFromXmlGregorianCalendarToLocalDateTime(saksmappe.getSaksdato()), DATO_OPPRETTET_SAK);
        assertEquals(saksmappe.getAdministrativEnhet(), NAV_KLAGEINSTANS);
        assertEquals(saksmappe.getSaksansvarlig(), OPPRETTET_AV_NAVN);
        assertEquals(saksmappe.getSaksstatus(), UNDER_BEHANDLING);
        assertSakspart(saksmappe.getPart());

    }

    private void assertRegistrering(List<Registrering> registreringList) {
        assertTrue(registreringList != null && registreringList.size() == 1);
        assertTrue(registreringList.get(0) instanceof Journalpost);

        Journalpost registreringJp = (Journalpost) registreringList.get(0);
        assertEquals(convertFromXmlGregorianCalendarToLocalDateTime(registreringJp.getOpprettetDato()), DATO_OPPRETTET_JOURNALPOST);
        assertEquals(registreringJp.getOpprettetAv(), OPPRETTET_AV_NAVN);
        assertDokumentbeskrivelseOpprettetAv(registreringJp.getDokumentbeskrivelse());
        assertEquals(registreringJp.getTittel(), TITTEL);
        assertEquals(registreringJp.getJournalposttype(), UTGAAENDE_DOKUMENT);
        assertEquals(registreringJp.getJournalstatus(), EKSPEDERT);
        assertEquals(convertFromXmlGregorianCalendarToLocalDateTime(registreringJp.getJournaldato()), DATO_JOURNALFOERT);
        assertKorrespondanseparter(registreringJp.getKorrespondansepart());
    }

    private void assertKorrespondanseparter(List<Korrespondansepart> korrespondansepartList) {
        assertTrue(korrespondansepartList != null && korrespondansepartList.size() == 2);

        Korrespondansepart mottaker = korrespondansepartList.get(0);
        assertEquals(mottaker.getKorrespondanseparttype(), MOTTAKER);
        assertEquals(mottaker.getKorrespondansepartNavn(), TRYGDERETTEN);

        Korrespondansepart avsender = korrespondansepartList.get(1);
        assertEquals(avsender.getKorrespondanseparttype(), AVSENDER);
        assertEquals(avsender.getKorrespondansepartNavn(), NAV_KLAGEINSTANS);
    }

    private void assertSakspart(List<Part> sakspartList) {
        assertTrue(sakspartList != null && sakspartList.size() == 2);

        Part sakspartAMP = sakspartList.get(0);
        assertNull(sakspartAMP.getPartID());
        assertEquals(sakspartAMP.getPartNavn(), NAV_KLAGEINSTANS);
        assertEquals(sakspartAMP.getPartRolle(), SAKSPART_ROLLE_AMP);
        assertEquals(sakspartAMP.getKontaktperson(), OPPRETTET_AV_NAVN);

        Part sakspartDAP = sakspartList.get(1);
        assertEquals(sakspartDAP.getPartNavn(), TPS_NAVN);
        assertEquals(sakspartDAP.getPartRolle(), SAKSPART_ROLLE_DAP);
        assertNull(sakspartDAP.getKontaktperson());
    }


    private void assertDokumentbeskrivelse(List<Dokumentbeskrivelse> dokumentbeskrivelseList) {
        assertTrue(dokumentbeskrivelseList != null && dokumentbeskrivelseList.size() == 2);

        assertTrue(dokumentbeskrivelseList.get(0) instanceof Dokumentbeskrivelse);
        Dokumentbeskrivelse dokumentbeskrivelseHoveddok = (Dokumentbeskrivelse) dokumentbeskrivelseList.get(0);
        assertEquals(dokumentbeskrivelseHoveddok.getTilknyttetRegistreringSom(), AvtaltmeldingConstant.HOVEDDOKUMENT);
        assertEquals(dokumentbeskrivelseHoveddok.getDokumentnummer(), BigInteger.ONE);
        assertCommonAttributesDokumentbeskrivelse(dokumentbeskrivelseHoveddok);
        assertEquals(dokumentbeskrivelseHoveddok.getTittel(), TITTEL_HOVEDDOK);
        assertEquals(convertFromXmlGregorianCalendarToLocalDateTime(dokumentbeskrivelseHoveddok.getOpprettetDato()), DATO_JOURNALFOERT);


        assertDokumentobjektHoveddokument(dokumentbeskrivelseHoveddok.getDokumentobjekt());

        assertTrue(dokumentbeskrivelseList.get(1) instanceof Dokumentbeskrivelse);
        Dokumentbeskrivelse dokumentbeskrivelseVedlegg = (Dokumentbeskrivelse) dokumentbeskrivelseList.get(1);
        assertEquals(dokumentbeskrivelseVedlegg.getTilknyttetRegistreringSom(), VEDLEGG);
        assertEquals(dokumentbeskrivelseVedlegg.getDokumentnummer(), BigInteger.valueOf(2));
        assertCommonAttributesVedleggDokumentbeskrivelse(dokumentbeskrivelseVedlegg);
        assertEquals(dokumentbeskrivelseVedlegg.getTittel(), TITTEL_VEDLEGG);
        assertEquals(convertFromXmlGregorianCalendarToLocalDateTime(dokumentbeskrivelseVedlegg.getOpprettetDato()), DATO_JOURNALFOERT);

        assertDokumentobjektVedlegg(dokumentbeskrivelseVedlegg.getDokumentobjekt());
    }

    private void assertDokumentbeskrivelseOpprettetAv(List<Dokumentbeskrivelse> dokumentbeskrivelseList) {
        assertTrue(dokumentbeskrivelseList != null && dokumentbeskrivelseList.size() == 2);

        assertTrue(dokumentbeskrivelseList.get(0) instanceof Dokumentbeskrivelse);
        Dokumentbeskrivelse dokumentbeskrivelseHoveddok = (Dokumentbeskrivelse) dokumentbeskrivelseList.get(0);
        assertEquals(dokumentbeskrivelseHoveddok.getTilknyttetRegistreringSom(), AvtaltmeldingConstant.HOVEDDOKUMENT);
        assertEquals(dokumentbeskrivelseHoveddok.getDokumentnummer(), BigInteger.ONE);
        assertCommonAttributesVedleggDokumentbeskrivelseOpprettetAv(dokumentbeskrivelseHoveddok);
        assertEquals(dokumentbeskrivelseHoveddok.getTittel(), TITTEL_HOVEDDOK);
        assertEquals(convertFromXmlGregorianCalendarToLocalDateTime(dokumentbeskrivelseHoveddok.getOpprettetDato()), DATO_JOURNALFOERT);


        assertDokumentobjektHoveddokument(dokumentbeskrivelseHoveddok.getDokumentobjekt());

        assertTrue(dokumentbeskrivelseList.get(1) instanceof Dokumentbeskrivelse);
        Dokumentbeskrivelse dokumentbeskrivelseVedlegg = (Dokumentbeskrivelse) dokumentbeskrivelseList.get(1);
        assertEquals(dokumentbeskrivelseVedlegg.getTilknyttetRegistreringSom(), VEDLEGG);
        assertEquals(dokumentbeskrivelseVedlegg.getDokumentnummer(), BigInteger.valueOf(2));
        assertCommonAttributesVedleggDokumentbeskrivelseOpprettetAv(dokumentbeskrivelseVedlegg);
        assertEquals(dokumentbeskrivelseVedlegg.getTittel(), TITTEL_VEDLEGG);
        assertEquals(convertFromXmlGregorianCalendarToLocalDateTime(dokumentbeskrivelseVedlegg.getOpprettetDato()), DATO_JOURNALFOERT);

        assertDokumentobjektVedlegg(dokumentbeskrivelseVedlegg.getDokumentobjekt());
    }


    private void assertDokumentobjektHoveddokument(List<Dokumentobjekt> dokumentobjektList) {
        assertTrue(dokumentobjektList != null && dokumentobjektList.size() == 1);
        Dokumentobjekt dokumentobjektHoveddok = dokumentobjektList.get(0);
        assertEquals(dokumentobjektHoveddok.getVersjonsnummer(), BigInteger.ONE);
        assertEquals(dokumentobjektHoveddok.getVariantformat(), DOKUMENT_HVOR_DELER_AV_INNHOLDET_ER_SKJERMET);
        assertEquals(convertFromXmlGregorianCalendarToLocalDateTime(dokumentobjektHoveddok.getOpprettetDato()), DATO_JOURNALFOERT);
        assertEquals(dokumentobjektHoveddok.getOpprettetAv(), JOURNALFOERT_AV_NAVN);
        assertEquals(dokumentobjektHoveddok.getReferanseDokumentfil(), JOURNALPOST_ID + "-" + DOKUMENT_INFO_ID_HOVEDDOK + "-" + DOKUMENT_HVOR_DELER_AV_INNHOLDET_ER_SKJERMET + "-" + FILTYPE_PDF);
    }


    private void assertDokumentobjektVedlegg(List<Dokumentobjekt> dokumentobjektList) {
        assertTrue(dokumentobjektList != null && dokumentobjektList.size() == 1);
        Dokumentobjekt dokumentobjektVedlegg = dokumentobjektList.get(0);
        assertEquals(dokumentobjektVedlegg.getVersjonsnummer(), BigInteger.ONE);
        assertEquals(dokumentobjektVedlegg.getVariantformat(), ARKIVFORMAT);
        assertEquals(convertFromXmlGregorianCalendarToLocalDateTime(dokumentobjektVedlegg.getOpprettetDato()), DATO_JOURNALFOERT);
        assertEquals(dokumentobjektVedlegg.getOpprettetAv(), JOURNALFOERT_AV_NAVN);
        assertEquals(dokumentobjektVedlegg.getReferanseDokumentfil(), JOURNALPOST_ID + "-" + DOKUMENT_INFO_ID_VEDLEGG + "-" + ARKIVFORMAT + "-" + FILTYPE_JPEG);
    }

    private void assertCommonAttributesDokumentbeskrivelse(Dokumentbeskrivelse dokumentbeskrivelse) {
        assertEquals(dokumentbeskrivelse.getDokumenttype(), DOKUMENTASJON);
        assertEquals(dokumentbeskrivelse.getDokumentstatus(), DOKUMENTET_ER_FERDIGSTILT);
        assertEquals(dokumentbeskrivelse.getOpprettetAv(), JOURNALFOERT_AV_NAVN);
        assertNotNull(dokumentbeskrivelse.getTilknyttetDato());
        assertEquals(dokumentbeskrivelse.getTilknyttetAv(), JOURNALFOERT_AV_NAVN);
    }

    private void assertCommonAttributesVedleggDokumentbeskrivelse(Dokumentbeskrivelse dokumentbeskrivelse) {
        assertEquals(dokumentbeskrivelse.getDokumenttype(), DOKUMENTASJON);
        assertEquals(dokumentbeskrivelse.getDokumentstatus(), DOKUMENTET_ER_FERDIGSTILT);
        assertEquals(dokumentbeskrivelse.getOpprettetAv(), AVSENDER_MOTTAKER_NAVN_ORIG_JP);
        assertNotNull(dokumentbeskrivelse.getTilknyttetDato());
        assertEquals(dokumentbeskrivelse.getTilknyttetAv(), JOURNALFOERT_AV_NAVN);
    }

    private void assertCommonAttributesVedleggDokumentbeskrivelseOpprettetAv(Dokumentbeskrivelse dokumentbeskrivelse) {
        assertEquals(dokumentbeskrivelse.getDokumenttype(), DOKUMENTASJON);
        assertEquals(dokumentbeskrivelse.getDokumentstatus(), DOKUMENTET_ER_FERDIGSTILT);
        assertEquals(dokumentbeskrivelse.getOpprettetAv(), JOURNALFOERT_AV_NAVN);
        assertNotNull(dokumentbeskrivelse.getTilknyttetDato());
        assertEquals(dokumentbeskrivelse.getTilknyttetAv(), JOURNALFOERT_AV_NAVN);
    }

    private JournalpostQdist013.JournalpostQdist013Builder createJournalpostQdist013Builder() {
        return JournalpostQdist013.builder()
                .journalpostId(JOURNALPOST_ID)
                .sak(JournalpostQdist013.Sak.builder()
                        .arkivsaksnummer(ARKIV_SAKNUMMER)
                        .datoOpprettet(DATO_OPPRETTET_SAK)
                        .build())
                .opprettetAvNavn(OPPRETTET_AV_NAVN)
                .bruker(JournalpostQdist013.Bruker.builder()
                        .id(BRUKER_ID_FNR)
                        .type(BRUKER_TYPE_FNR)
                        .build())
                .datoOpprettet(DATO_OPPRETTET_JOURNALPOST)
                .tittel(TITTEL)
                .journalfortAvNavn(JOURNALFOERT_AV_NAVN)
                .temanavn(TEMA_NAVN)
                .journalposttype(UTGAAENDE)
                .relevanteDatoer(Collections.singletonList(JournalpostQdist013.RelevantDato.builder()
                        .datotype(JournalpostQdist013.Datotype.DATO_JOURNALFOERT)
                        .dato(DATO_JOURNALFOERT)
                        .build()))
                .dokumenter(Arrays.asList(createHoveddokumentBuilder().build(), createVedleggBuilder().build()
                ));
    }

    private JournalpostQdist013.JournalpostQdist013Builder createJournalpostQdist013BuilderNoJournalFoertAv() {
        return JournalpostQdist013.builder()
                .journalpostId(JOURNALPOST_ID)
                .sak(JournalpostQdist013.Sak.builder()
                        .arkivsaksnummer(ARKIV_SAKNUMMER)
                        .datoOpprettet(DATO_OPPRETTET_SAK)
                        .build())
                .opprettetAvNavn(OPPRETTET_AV_NAVN)
                .bruker(JournalpostQdist013.Bruker.builder()
                        .id(BRUKER_ID_FNR)
                        .type(BRUKER_TYPE_FNR)
                        .build())
                .datoOpprettet(DATO_OPPRETTET_JOURNALPOST)
                .tittel(TITTEL)
                .temanavn(TEMA_NAVN)
                .journalposttype(UTGAAENDE)
                .relevanteDatoer(Collections.singletonList(JournalpostQdist013.RelevantDato.builder()
                        .datotype(JournalpostQdist013.Datotype.DATO_JOURNALFOERT)
                        .dato(DATO_JOURNALFOERT)
                        .build()))
                .dokumenter(Arrays.asList(createHoveddokumentBuilder().build(), createVedleggBuilder().build()
                ));
    }

    private JournalpostQdist013.DokumentInfo.DokumentInfoBuilder createHoveddokumentBuilder() {
        return JournalpostQdist013.DokumentInfo.builder()
                .dokumentInfoId(DOKUMENT_INFO_ID_HOVEDDOK)
                .dokumentstatus(FERDIGSTILT)
                .tittel(TITTEL_HOVEDDOK)
                .originalJournalpostId(null)
                .dokumentvarianter(Arrays.asList(JournalpostQdist013.DokumentInfo.Dokumentvariant.builder()
                                .variantformat(VARIANTFORMAT_ARKIV)
                                .filtype(FILTYPE_PNG)
                                .build(),
                        JournalpostQdist013.DokumentInfo.Dokumentvariant.builder()
                                .variantformat(VARIANTFORMAT_SLADDET)
                                .filtype(FILTYPE_PDF)
                                .build()));
    }

    private JournalpostQdist013.DokumentInfo.DokumentInfoBuilder createVedleggBuilderUtenDato() {
        return JournalpostQdist013.DokumentInfo.builder()
                .dokumentInfoId(DOKUMENT_INFO_ID_VEDLEGG)
                .dokumentstatus(FERDIGSTILT)
                .tittel(TITTEL_VEDLEGG)
                .originalJournalpostId(null)
                .dokumentvarianter(Arrays.asList(JournalpostQdist013.DokumentInfo.Dokumentvariant.builder()
                                .variantformat(VARIANTFORMAT_ARKIV)
                                .filtype(FILTYPE_JPEG)
                                .build(),
                        JournalpostQdist013.DokumentInfo.Dokumentvariant.builder()
                                .variantformat(VARIANTFORMAT_PRODUKSJON)
                                .filtype(FILTYPE_PDF)
                                .build()));
    }

    private JournalpostQdist013.DokumentInfo.DokumentInfoBuilder createVedleggBuilder() {
        return JournalpostQdist013.DokumentInfo.builder()
                .dokumentInfoId(DOKUMENT_INFO_ID_VEDLEGG)
                .dokumentstatus(FERDIGSTILT)
                .tittel(TITTEL_VEDLEGG)
                .originalJournalpostId(null)
                .dokumentvarianter(Arrays.asList(JournalpostQdist013.DokumentInfo.Dokumentvariant.builder()
                                .variantformat(VARIANTFORMAT_ARKIV)
                                .filtype(FILTYPE_JPEG)
                                .build(),
                        JournalpostQdist013.DokumentInfo.Dokumentvariant.builder()
                                .variantformat(VARIANTFORMAT_PRODUKSJON)
                                .filtype(FILTYPE_PDF)
                                .build()));
    }

    private LightweightSafJournalpostQdist013 createLightweightSafJournalpostQdist013() {
        return LightweightSafJournalpostQdist013.builder()
                .avsenderMottakerNavn(AVSENDER_MOTTAKER_NAVN_ORIG_JP)
                .journalfortAvNavn(JOURNALFOERT_AV_NAVN_ORIG_JP)
                .datoJournalfoert(DATO_JOURNALFOERT_ORIG_JP)
                .build();
    }

    private LightweightSafJournalpostQdist013 createLightweightSafJournalpostQdist013NoDatoJournal() {
        return LightweightSafJournalpostQdist013.builder()
                .avsenderMottakerNavn(AVSENDER_MOTTAKER_NAVN_ORIG_JP)
                .journalfortAvNavn(JOURNALFOERT_AV_NAVN_ORIG_JP)
                .datoJournalfoert(LocalDateTime.now().minusHours(5))
                .build();
    }

    private LightweightSafJournalpostQdist013 createLightweightSafJournalpostQdist013NoJournalFoertAv() {
        return LightweightSafJournalpostQdist013.builder()
                .avsenderMottakerNavn(AVSENDER_MOTTAKER_NAVN_ORIG_JP)
                .datoJournalfoert(DATO_JOURNALFOERT_ORIG_JP)
                .build();
    }

    private boolean isUuid(String uuidCandidate) {
        try {
            UUID.fromString(uuidCandidate);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}