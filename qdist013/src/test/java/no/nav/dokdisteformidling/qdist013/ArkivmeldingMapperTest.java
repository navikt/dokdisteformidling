package no.nav.dokdisteformidling.qdist013;

import static no.nav.dokdisteformidling.constants.DomainConstants.APP_NAME;
import static no.nav.dokdisteformidling.constants.DomainConstants.VARIANTFORMAT_ARKIV;
import static no.nav.dokdisteformidling.constants.DomainConstants.VARIANTFORMAT_PRODUKSJON;
import static no.nav.dokdisteformidling.constants.DomainConstants.VARIANTFORMAT_SLADDET;
import static no.nav.dokdisteformidling.qdist013.ArkivmeldingMapper.DOKUMENTTYPE_DOKUMENTASJON;
import static no.nav.dokdisteformidling.qdist013.ArkivmeldingMapper.FERDIGSTILT;
import static no.nav.dokdisteformidling.qdist013.ArkivmeldingMapper.INNGAAENDE;
import static no.nav.dokdisteformidling.qdist013.ArkivmeldingMapper.NAV_KLAGEINSTANS;
import static no.nav.dokdisteformidling.qdist013.ArkivmeldingMapper.SAKSPART_ROLLE_AMP;
import static no.nav.dokdisteformidling.qdist013.ArkivmeldingMapper.SAKSPART_ROLLE_DAP;
import static no.nav.dokdisteformidling.qdist013.ArkivmeldingMapper.TRYGDERETTEN;
import static no.nav.dokdisteformidling.qdist013.ArkivmeldingMapper.UTGAAENDE;
import static no.nav.dokdisteformidling.qdist013.TestUtil.convertFromXmlGregorianCalendarToLocalDateTime;
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

import no.arkivverket.standarder.noark5.arkivmelding.Arkivmelding;
import no.arkivverket.standarder.noark5.arkivmelding.Basisregistrering;
import no.arkivverket.standarder.noark5.arkivmelding.Dokumentbeskrivelse;
import no.arkivverket.standarder.noark5.arkivmelding.Dokumentobjekt;
import no.arkivverket.standarder.noark5.arkivmelding.Journalpost;
import no.arkivverket.standarder.noark5.arkivmelding.Korrespondansepart;
import no.arkivverket.standarder.noark5.arkivmelding.Mappe;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.xml.bind.JAXBElement;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
class ArkivmeldingMapperTest {

	private static final String BESTILLINGS_ID = "bestillingsId";
	private static final String JOURNALPOST_ID = "987654321";

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


	private Aktoerregister aktoerregisterMock = mock(Aktoerregister.class);
	private Ereg eregMock = mock(Ereg.class);
	private Tps tpsMock = mock(Tps.class);
	private SafJournalpostQueryService safJournalpostQueryServiceMock = mock(SafJournalpostQueryService.class);
	private ArkivmeldingMapper arkivmeldingMapper = new ArkivmeldingMapper(safJournalpostQueryServiceMock, aktoerregisterMock, eregMock, tpsMock);

	@Test
	@DisplayName("Asserts all fields")
	void fullHappyPath() {
		when(tpsMock.hentNavn(any(String.class))).thenReturn(TPS_NAVN);
		JAXBElement<Arkivmelding> arkivmeldingJAXBElement = arkivmeldingMapper.createArkivMelding(createJournalpostQdist013Builder()
				.build(), BESTILLINGS_ID);

		assertThat(arkivmeldingJAXBElement, notNullValue());
		Arkivmelding arkivmelding = arkivmeldingJAXBElement.getValue();
		assertArkivmelding(arkivmelding);

		verify(tpsMock, times(1)).hentNavn(BRUKER_ID_FNR);
		verify(eregMock, times(0)).hentNavn(any(String.class));
		verify(safJournalpostQueryServiceMock, times(0)).hentJournalpost(any(String.class));

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

		JAXBElement<Arkivmelding> arkivmeldingJAXBElement = arkivmeldingMapper.createArkivMelding(journalpostQdist013, BESTILLINGS_ID);
		Arkivmelding arkivmelding = arkivmeldingJAXBElement.getValue();
		List<Mappe> mappeList = arkivmelding.getMappe();
		Saksmappe saksmappe = (Saksmappe) mappeList.get(0);
		Sakspart sakspartDAP = saksmappe.getSakspart().get(1);

		assertEquals(sakspartDAP.getSakspartID(), BRUKER_ID_ORGNR);
		assertEquals(sakspartDAP.getSakspartNavn(), EREG_NAVN);
		assertEquals(sakspartDAP.getSakspartRolle(), SAKSPART_ROLLE_DAP);
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

		JAXBElement<Arkivmelding> arkivmeldingJAXBElement = arkivmeldingMapper.createArkivMelding(journalpostQdist013, BESTILLINGS_ID);
		Arkivmelding arkivmelding = arkivmeldingJAXBElement.getValue();
		List<Mappe> mappeList = arkivmelding.getMappe();
		Saksmappe saksmappe = (Saksmappe) mappeList.get(0);
		Sakspart sakspartDAP = saksmappe.getSakspart().get(1);

		assertEquals(sakspartDAP.getSakspartID(), FNR_FOR_AKTOER_ID);
		assertEquals(sakspartDAP.getSakspartNavn(), TPS_NAVN);
		assertEquals(sakspartDAP.getSakspartRolle(), SAKSPART_ROLLE_DAP);
		assertNull(sakspartDAP.getKontaktperson());

		verify(aktoerregisterMock, times(1)).hentIdentForAktoerId(BRUKER_ID_AKTOER_ID);
		verify(tpsMock, times(1)).hentNavn(FNR_FOR_AKTOER_ID);
		verify(eregMock, times(0)).hentNavn(any(String.class));
		verify(safJournalpostQueryServiceMock, times(0)).hentJournalpost(any(String.class));
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

		JAXBElement<Arkivmelding> arkivmeldingJAXBElement = arkivmeldingMapper.createArkivMelding(journalpostQdist013, BESTILLINGS_ID);
		Arkivmelding arkivmelding = arkivmeldingJAXBElement.getValue();
		List<Mappe> mappeList = arkivmelding.getMappe();
		Saksmappe saksmappe = (Saksmappe) mappeList.get(0);
		Journalpost basisregistreringJp = (Journalpost) saksmappe.getBasisregistrering().get(0);
		Dokumentbeskrivelse dokumentbeskrivelse = (Dokumentbeskrivelse) basisregistreringJp.getDokumentbeskrivelseAndDokumentobjekt()
				.get(1);

		assertEquals(dokumentbeskrivelse.getTittel(), TITTEL_VEDLEGG + ", Fra " + AVSENDER_MOTTAKER_NAVN_ORIG_JP);

		verify(safJournalpostQueryServiceMock, times(5)).hentJournalpost(ORIGINAL_JPID_VEDLEGG);
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

		JAXBElement<Arkivmelding> arkivmeldingJAXBElement = arkivmeldingMapper.createArkivMelding(journalpostQdist013, BESTILLINGS_ID);
		Arkivmelding arkivmelding = arkivmeldingJAXBElement.getValue();
		List<Mappe> mappeList = arkivmelding.getMappe();
		Saksmappe saksmappe = (Saksmappe) mappeList.get(0);
		Journalpost basisregistreringJp = (Journalpost) saksmappe.getBasisregistrering().get(0);
		Dokumentbeskrivelse dokumentbeskrivelse = (Dokumentbeskrivelse) basisregistreringJp.getDokumentbeskrivelseAndDokumentobjekt()
				.get(1);

		assertEquals(dokumentbeskrivelse.getTittel(), TITTEL_VEDLEGG + ", Til " + AVSENDER_MOTTAKER_NAVN_ORIG_JP);

		verify(safJournalpostQueryServiceMock, times(5)).hentJournalpost(ORIGINAL_JPID_VEDLEGG);
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

		JAXBElement<Arkivmelding> arkivmeldingJAXBElement = arkivmeldingMapper.createArkivMelding(journalpostQdist013, BESTILLINGS_ID);
		Arkivmelding arkivmelding = arkivmeldingJAXBElement.getValue();
		List<Mappe> mappeList = arkivmelding.getMappe();
		Saksmappe saksmappe = (Saksmappe) mappeList.get(0);
		Journalpost basisregistreringJp = (Journalpost) saksmappe.getBasisregistrering().get(0);
		Dokumentbeskrivelse dokumentbeskrivelse = (Dokumentbeskrivelse) basisregistreringJp.getDokumentbeskrivelseAndDokumentobjekt()
				.get(1);

		assertEquals(dokumentbeskrivelse.getTittel(), TITTEL_VEDLEGG);

		verify(safJournalpostQueryServiceMock, times(4)).hentJournalpost(ORIGINAL_JPID_VEDLEGG);
	}

	@Test
	@DisplayName("Case when vedlegg has original jpId. Should get opprettet dato from original journalpost")
	void happyPathTestOpprettetDatoWhenVedleggHasOriginalJpId() {
		when(safJournalpostQueryServiceMock.hentJournalpost(ORIGINAL_JPID_VEDLEGG)).thenReturn(createLightweightSafJournalpostQdist013());

		JournalpostQdist013 journalpostQdist013 = createJournalpostQdist013Builder()
				.dokumenter(Arrays.asList(createHoveddokumentBuilder().build(),
						createVedleggBuilder().originalJournalpostId(ORIGINAL_JPID_VEDLEGG).build()))
				.build();

		JAXBElement<Arkivmelding> arkivmeldingJAXBElement = arkivmeldingMapper.createArkivMelding(journalpostQdist013, BESTILLINGS_ID);
		Arkivmelding arkivmelding = arkivmeldingJAXBElement.getValue();
		List<Mappe> mappeList = arkivmelding.getMappe();
		Saksmappe saksmappe = (Saksmappe) mappeList.get(0);
		Journalpost basisregistreringJp = (Journalpost) saksmappe.getBasisregistrering().get(0);

		Dokumentbeskrivelse dokumentbeskrivelseHoveddok = (Dokumentbeskrivelse) basisregistreringJp.getDokumentbeskrivelseAndDokumentobjekt()
				.get(0);
		Dokumentobjekt dokumentobjektHoveddok = dokumentbeskrivelseHoveddok.getDokumentobjekt().get(0);
		Dokumentbeskrivelse dokumentbeskrivelseVedlegg = (Dokumentbeskrivelse) basisregistreringJp.getDokumentbeskrivelseAndDokumentobjekt()
				.get(1);
		Dokumentobjekt dokumentobjektVedlegg = dokumentbeskrivelseVedlegg.getDokumentobjekt().get(0);

		assertEquals(convertFromXmlGregorianCalendarToLocalDateTime(dokumentbeskrivelseHoveddok.getOpprettetDato()), DATO_JOURNALFOERT);
		assertEquals(convertFromXmlGregorianCalendarToLocalDateTime(dokumentobjektHoveddok.getOpprettetDato()), DATO_JOURNALFOERT);
		assertEquals(convertFromXmlGregorianCalendarToLocalDateTime(dokumentbeskrivelseVedlegg.getOpprettetDato()), DATO_JOURNALFOERT_ORIG_JP);
		assertEquals(convertFromXmlGregorianCalendarToLocalDateTime(dokumentobjektVedlegg.getOpprettetDato()), DATO_JOURNALFOERT_ORIG_JP);

		verify(safJournalpostQueryServiceMock, times(5)).hentJournalpost(ORIGINAL_JPID_VEDLEGG);
	}

	@Test
	@DisplayName("Case when vedlegg has original jpId. Should get opprettet av from original journalpost")
	void happyPathTestOpprettetAvWhenVedleggHasOriginalJpId() {
		when(safJournalpostQueryServiceMock.hentJournalpost(ORIGINAL_JPID_VEDLEGG)).thenReturn(createLightweightSafJournalpostQdist013());

		JournalpostQdist013 journalpostQdist013 = createJournalpostQdist013Builder()
				.dokumenter(Arrays.asList(createHoveddokumentBuilder().build(),
						createVedleggBuilder().originalJournalpostId(ORIGINAL_JPID_VEDLEGG).build()))
				.build();

		JAXBElement<Arkivmelding> arkivmeldingJAXBElement = arkivmeldingMapper.createArkivMelding(journalpostQdist013, BESTILLINGS_ID);
		Arkivmelding arkivmelding = arkivmeldingJAXBElement.getValue();
		List<Mappe> mappeList = arkivmelding.getMappe();
		Saksmappe saksmappe = (Saksmappe) mappeList.get(0);
		Journalpost basisregistreringJp = (Journalpost) saksmappe.getBasisregistrering().get(0);

		Dokumentbeskrivelse dokumentbeskrivelseHoveddok = (Dokumentbeskrivelse) basisregistreringJp.getDokumentbeskrivelseAndDokumentobjekt()
				.get(0);
		Dokumentobjekt dokumentobjektHoveddok = dokumentbeskrivelseHoveddok.getDokumentobjekt().get(0);
		Dokumentbeskrivelse dokumentbeskrivelseVedlegg = (Dokumentbeskrivelse) basisregistreringJp.getDokumentbeskrivelseAndDokumentobjekt()
				.get(1);
		Dokumentobjekt dokumentobjektVedlegg = dokumentbeskrivelseVedlegg.getDokumentobjekt().get(0);

		assertEquals(dokumentbeskrivelseHoveddok.getOpprettetAv(), JOURNALFOERT_AV_NAVN);
		assertEquals(dokumentobjektHoveddok.getOpprettetAv(), JOURNALFOERT_AV_NAVN);
		assertEquals(dokumentbeskrivelseVedlegg.getOpprettetAv(), JOURNALFOERT_AV_NAVN_ORIG_JP);
		assertEquals(dokumentobjektVedlegg.getOpprettetAv(), JOURNALFOERT_AV_NAVN_ORIG_JP);

		verify(safJournalpostQueryServiceMock, times(5)).hentJournalpost(ORIGINAL_JPID_VEDLEGG);
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

		JAXBElement<Arkivmelding> arkivmeldingJAXBElement = arkivmeldingMapper.createArkivMelding(journalpostQdist013, BESTILLINGS_ID);
		Arkivmelding arkivmelding = arkivmeldingJAXBElement.getValue();
		List<Mappe> mappeList = arkivmelding.getMappe();
		Saksmappe saksmappe = (Saksmappe) mappeList.get(0);
		Journalpost basisregistreringJp = (Journalpost) saksmappe.getBasisregistrering().get(0);

		Dokumentbeskrivelse dokumentbeskrivelseHoveddok = (Dokumentbeskrivelse) basisregistreringJp.getDokumentbeskrivelseAndDokumentobjekt()
				.get(0);
		Dokumentobjekt dokumentobjektHoveddok = dokumentbeskrivelseHoveddok.getDokumentobjekt().get(0);

		assertEquals(dokumentobjektHoveddok.getVariantformat(), Variantformat.PRODUKSJONSFORMAT);
		assertTrue(dokumentobjektHoveddok.getReferanseDokumentfil().contains(VARIANTFORMAT_PRODUKSJON));
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

		JAXBElement<Arkivmelding> arkivmeldingJAXBElement = arkivmeldingMapper.createArkivMelding(journalpostQdist013, BESTILLINGS_ID);
		Arkivmelding arkivmelding = arkivmeldingJAXBElement.getValue();
		List<Mappe> mappeList = arkivmelding.getMappe();
		Saksmappe saksmappe = (Saksmappe) mappeList.get(0);
		Journalpost basisregistreringJp = (Journalpost) saksmappe.getBasisregistrering().get(0);

		Dokumentbeskrivelse dokumentbeskrivelseHoveddok = (Dokumentbeskrivelse) basisregistreringJp.getDokumentbeskrivelseAndDokumentobjekt()
				.get(0);
		Dokumentobjekt dokumentobjektHoveddok = dokumentbeskrivelseHoveddok.getDokumentobjekt().get(0);

		assertEquals(dokumentobjektHoveddok.getVariantformat(), Variantformat.ARKIVFORMAT);
		assertTrue(dokumentobjektHoveddok.getReferanseDokumentfil().contains(VARIANTFORMAT_ARKIV));
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

		JAXBElement<Arkivmelding> arkivmeldingJAXBElement = arkivmeldingMapper.createArkivMelding(journalpostQdist013, BESTILLINGS_ID);
		Arkivmelding arkivmelding = arkivmeldingJAXBElement.getValue();
		List<Mappe> mappeList = arkivmelding.getMappe();
		Saksmappe saksmappe = (Saksmappe) mappeList.get(0);
		Journalpost basisregistreringJp = (Journalpost) saksmappe.getBasisregistrering().get(0);

		Dokumentbeskrivelse dokumentbeskrivelseHoveddok = (Dokumentbeskrivelse) basisregistreringJp.getDokumentbeskrivelseAndDokumentobjekt()
				.get(0);
		Dokumentobjekt dokumentobjektHoveddok = dokumentbeskrivelseHoveddok.getDokumentobjekt().get(0);

		assertEquals(dokumentobjektHoveddok.getVariantformat(), Variantformat.ARKIVFORMAT);
		assertTrue(dokumentobjektHoveddok.getReferanseDokumentfil().contains(VARIANTFORMAT_ARKIV));
	}

	@Test
	@DisplayName("Case when vedlegg has no dokumentstatus set. That vedlegg should be considered FERDIGSTILT.")
	void happyPathVedleggFerdigstiltUtenStatus() {
		JournalpostQdist013 journalpostQdist013 = createJournalpostQdist013Builder()
				.dokumenter(Arrays.asList(createHoveddokumentBuilder().build(),
						createVedleggBuilder().dokumentstatus(null).build()))
				.build();

		JAXBElement<Arkivmelding> arkivmeldingJAXBElement = arkivmeldingMapper.createArkivMelding(journalpostQdist013, BESTILLINGS_ID);
		Arkivmelding arkivmelding = arkivmeldingJAXBElement.getValue();
		List<Mappe> mappeList = arkivmelding.getMappe();
		Saksmappe saksmappe = (Saksmappe) mappeList.get(0);
		Journalpost basisregistreringJp = (Journalpost) saksmappe.getBasisregistrering().get(0);

		assertEquals(2, arkivmelding.getAntallFiler());
		assertDokumentbeskrivelse(basisregistreringJp.getDokumentbeskrivelseAndDokumentobjekt());
	}

	@Test
	@DisplayName("Case when vedlegg does not have dokumentstatus FERDIGSTILT. That vedlegg should not be mapped.")
	void happyPathIkkeFerdigstiltVedlegg() {
		JournalpostQdist013 journalpostQdist013 = createJournalpostQdist013Builder()
				.dokumenter(Arrays.asList(createHoveddokumentBuilder().build(),
						createVedleggBuilder().dokumentInfoId(DOKUMENT_INFO_ID_VEDLEGG_2).dokumentstatus("UNDER_REDIGERING").build(),
						createVedleggBuilder().build()))
				.build();

		JAXBElement<Arkivmelding> arkivmeldingJAXBElement = arkivmeldingMapper.createArkivMelding(journalpostQdist013, BESTILLINGS_ID);
		Arkivmelding arkivmelding = arkivmeldingJAXBElement.getValue();
		List<Mappe> mappeList = arkivmelding.getMappe();
		Saksmappe saksmappe = (Saksmappe) mappeList.get(0);
		Journalpost basisregistreringJp = (Journalpost) saksmappe.getBasisregistrering().get(0);

		assertEquals(2, arkivmelding.getAntallFiler());
		assertDokumentbeskrivelse(basisregistreringJp.getDokumentbeskrivelseAndDokumentobjekt());
	}

	@Test
	@DisplayName("Case for satt originalJournalPostId men ukjent journalfører")
	void ukjentJournalFoertAv() {
		when(safJournalpostQueryServiceMock.hentJournalpost(ORIGINAL_JPID_VEDLEGG)).thenReturn(createLightweightSafJournalpostQdist013NoJournalFoertAv());

		JournalpostQdist013 journalpostQdist013 = createJournalpostQdist013Builder()
				.dokumenter(Arrays.asList(createHoveddokumentBuilder().build(),
						createVedleggBuilder().originalJournalpostId(ORIGINAL_JPID_VEDLEGG).build()))
				.build();

		JAXBElement<Arkivmelding> arkivmeldingJAXBElement = arkivmeldingMapper.createArkivMelding(journalpostQdist013, BESTILLINGS_ID);
		Journalpost basisregistreringJp = (Journalpost) arkivmeldingJAXBElement.getValue().getMappe().get(0).getBasisregistrering().get(0);
		Dokumentbeskrivelse dokumentbeskrivelseVedlegg = (Dokumentbeskrivelse) basisregistreringJp.getDokumentbeskrivelseAndDokumentobjekt()
				.get(1);
		Dokumentobjekt dokumentobjektVedlegg = dokumentbeskrivelseVedlegg.getDokumentobjekt().get(0);

		assertEquals(dokumentobjektVedlegg.getOpprettetAv(), OPPRETTET_AV_UKJENT);
		assertEquals(dokumentbeskrivelseVedlegg.getOpprettetAv(), OPPRETTET_AV_UKJENT);
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

		assertTrue(isUuid(saksmappe.getSystemID()));
		assertEquals(saksmappe.getTittel(), TEMA_NAVN);
		assertEquals(convertFromXmlGregorianCalendarToLocalDateTime(saksmappe.getOpprettetDato()), DATO_OPPRETTET_SAK);
		assertEquals(saksmappe.getOpprettetAv(), OPPRETTET_AV_NAVN);
		assertBasisregistrering(saksmappe.getBasisregistrering());
		assertEquals(convertFromXmlGregorianCalendarToLocalDateTime(saksmappe.getSaksdato()), DATO_OPPRETTET_SAK);
		assertEquals(saksmappe.getAdministrativEnhet(), NAV_KLAGEINSTANS);
		assertEquals(saksmappe.getSaksansvarlig(), OPPRETTET_AV_NAVN);
		assertEquals(saksmappe.getSaksstatus(), Saksstatus.UNDER_BEHANDLING);
		assertSakspart(saksmappe.getSakspart());

	}

	private void assertBasisregistrering(List<Basisregistrering> basisregistreringList) {
		assertTrue(basisregistreringList != null && basisregistreringList.size() == 1);
		assertTrue(basisregistreringList.get(0) instanceof Journalpost);

		Journalpost basisregistreringJp = (Journalpost) basisregistreringList.get(0);
		assertTrue(isUuid(basisregistreringJp.getSystemID()));
		assertEquals(convertFromXmlGregorianCalendarToLocalDateTime(basisregistreringJp.getOpprettetDato()), DATO_OPPRETTET_JOURNALPOST);
		assertEquals(basisregistreringJp.getOpprettetAv(), OPPRETTET_AV_NAVN);
		assertTrue(isUuid(basisregistreringJp.getReferanseForelderMappe()));
		assertDokumentbeskrivelse(basisregistreringJp.getDokumentbeskrivelseAndDokumentobjekt());
		assertEquals(basisregistreringJp.getTittel(), TITTEL);
		assertEquals(basisregistreringJp.getJournalposttype(), Journalposttype.UTGÅENDE_DOKUMENT);
		assertEquals(basisregistreringJp.getJournalstatus(), Journalstatus.EKSPEDERT);
		assertEquals(convertFromXmlGregorianCalendarToLocalDateTime(basisregistreringJp.getJournaldato()), DATO_JOURNALFOERT);
		assertKorrespondanseparter(basisregistreringJp.getKorrespondansepart());
	}

	private void assertKorrespondanseparter(List<Korrespondansepart> korrespondansepartList) {
		assertTrue(korrespondansepartList != null && korrespondansepartList.size() == 2);

		Korrespondansepart mottaker = korrespondansepartList.get(0);
		assertEquals(mottaker.getKorrespondanseparttype(), Korrespondanseparttype.MOTTAKER);
		assertEquals(mottaker.getKorrespondansepartNavn(), TRYGDERETTEN);

		Korrespondansepart avsender = korrespondansepartList.get(1);
		assertEquals(avsender.getKorrespondanseparttype(), Korrespondanseparttype.AVSENDER);
		assertEquals(avsender.getKorrespondansepartNavn(), NAV_KLAGEINSTANS);
	}

	private void assertSakspart(List<Sakspart> sakspartList) {
		assertTrue(sakspartList != null && sakspartList.size() == 2);

		Sakspart sakspartAMP = sakspartList.get(0);
		assertNull(sakspartAMP.getSakspartID());
		assertEquals(sakspartAMP.getSakspartNavn(), NAV_KLAGEINSTANS);
		assertEquals(sakspartAMP.getSakspartRolle(), SAKSPART_ROLLE_AMP);
		assertEquals(sakspartAMP.getKontaktperson(), OPPRETTET_AV_NAVN);

		Sakspart sakspartDAP = sakspartList.get(1);
		assertEquals(sakspartDAP.getSakspartID(), BRUKER_ID_FNR);
		assertEquals(sakspartDAP.getSakspartNavn(), TPS_NAVN);
		assertEquals(sakspartDAP.getSakspartRolle(), SAKSPART_ROLLE_DAP);
		assertNull(sakspartDAP.getKontaktperson());
	}


	private void assertDokumentbeskrivelse(List<Object> dokumentbeskrivelseList) {
		assertTrue(dokumentbeskrivelseList != null && dokumentbeskrivelseList.size() == 2);

		assertTrue(dokumentbeskrivelseList.get(0) instanceof Dokumentbeskrivelse);
		Dokumentbeskrivelse dokumentbeskrivelseHoveddok = (Dokumentbeskrivelse) dokumentbeskrivelseList.get(0);
		assertEquals(dokumentbeskrivelseHoveddok.getTilknyttetRegistreringSom(), TilknyttetRegistreringSom.HOVEDDOKUMENT);
		assertEquals(dokumentbeskrivelseHoveddok.getDokumentnummer(), BigInteger.ONE);
		assertCommonAttributesDokumentbeskrivelse(dokumentbeskrivelseHoveddok);
		assertEquals(dokumentbeskrivelseHoveddok.getTittel(), TITTEL_HOVEDDOK);
		assertEquals(convertFromXmlGregorianCalendarToLocalDateTime(dokumentbeskrivelseHoveddok.getOpprettetDato()), DATO_JOURNALFOERT);


		assertDokumentobjektHoveddokument(dokumentbeskrivelseHoveddok.getDokumentobjekt());

		assertTrue(dokumentbeskrivelseList.get(1) instanceof Dokumentbeskrivelse);
		Dokumentbeskrivelse dokumentbeskrivelseVedlegg = (Dokumentbeskrivelse) dokumentbeskrivelseList.get(1);
		assertEquals(dokumentbeskrivelseVedlegg.getTilknyttetRegistreringSom(), TilknyttetRegistreringSom.VEDLEGG);
		assertEquals(dokumentbeskrivelseVedlegg.getDokumentnummer(), BigInteger.valueOf(2));
		assertCommonAttributesDokumentbeskrivelse(dokumentbeskrivelseVedlegg);
		assertEquals(dokumentbeskrivelseVedlegg.getTittel(), TITTEL_VEDLEGG);
		assertEquals(convertFromXmlGregorianCalendarToLocalDateTime(dokumentbeskrivelseVedlegg.getOpprettetDato()), DATO_JOURNALFOERT);

		assertDokumentobjektVedlegg(dokumentbeskrivelseVedlegg.getDokumentobjekt());
	}

	private void assertDokumentobjektHoveddokument(List<Dokumentobjekt> dokumentobjektList) {
		assertTrue(dokumentobjektList != null && dokumentobjektList.size() == 1);
		Dokumentobjekt dokumentobjektHoveddok = dokumentobjektList.get(0);
		assertEquals(dokumentobjektHoveddok.getVersjonsnummer(), BigInteger.ONE);
		assertEquals(dokumentobjektHoveddok.getVariantformat(), Variantformat.DOKUMENT_HVOR_DELER_AV_INNHOLDET_ER_SKJERMET);
		assertEquals(convertFromXmlGregorianCalendarToLocalDateTime(dokumentobjektHoveddok.getOpprettetDato()), DATO_JOURNALFOERT);
		assertEquals(dokumentobjektHoveddok.getOpprettetAv(), JOURNALFOERT_AV_NAVN);
		assertEquals(dokumentobjektHoveddok.getReferanseDokumentfil(), JOURNALPOST_ID + "-" + DOKUMENT_INFO_ID_HOVEDDOK + "-" + VARIANTFORMAT_SLADDET + "-" + FILTYPE_PDF);
	}


	private void assertDokumentobjektVedlegg(List<Dokumentobjekt> dokumentobjektList) {
		assertTrue(dokumentobjektList != null && dokumentobjektList.size() == 1);
		Dokumentobjekt dokumentobjektVedlegg = dokumentobjektList.get(0);
		assertEquals(dokumentobjektVedlegg.getVersjonsnummer(), BigInteger.ONE);
		assertEquals(dokumentobjektVedlegg.getVariantformat(), Variantformat.ARKIVFORMAT);
		assertEquals(convertFromXmlGregorianCalendarToLocalDateTime(dokumentobjektVedlegg.getOpprettetDato()), DATO_JOURNALFOERT);
		assertEquals(dokumentobjektVedlegg.getOpprettetAv(), JOURNALFOERT_AV_NAVN);
		assertEquals(dokumentobjektVedlegg.getReferanseDokumentfil(), JOURNALPOST_ID + "-" + DOKUMENT_INFO_ID_VEDLEGG + "-" + VARIANTFORMAT_ARKIV + "-" + FILTYPE_JPEG);
	}

	private void assertCommonAttributesDokumentbeskrivelse(Dokumentbeskrivelse dokumentbeskrivelse) {
		assertTrue(isUuid(dokumentbeskrivelse.getSystemID()));
		assertEquals(dokumentbeskrivelse.getDokumenttype(), DOKUMENTTYPE_DOKUMENTASJON);
		assertEquals(dokumentbeskrivelse.getDokumentstatus(), Dokumentstatus.DOKUMENTET_ER_FERDIGSTILT);
		assertEquals(dokumentbeskrivelse.getOpprettetAv(), JOURNALFOERT_AV_NAVN);
		assertNotNull(dokumentbeskrivelse.getTilknyttetDato());
		assertEquals(dokumentbeskrivelse.getTilknyttetAv(), JOURNALFOERT_AV_NAVN);
	}

	private JournalpostQdist013.JournalpostQdist013Builder createJournalpostQdist013Builder() {
		return JournalpostQdist013.builder()
				.journalpostId(JOURNALPOST_ID)
				.sak(JournalpostQdist013.Sak.builder()
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

	private LightweightSafJournalpostQdist013 createLightweightSafJournalpostQdist013NoJournalFoertAv() {
		return LightweightSafJournalpostQdist013.builder()
				.avsenderMottakerNavn(AVSENDER_MOTTAKER_NAVN_ORIG_JP)
				//.journalfortAvNavn(null)
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