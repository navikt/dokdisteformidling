package no.nav.dokdisteformidling.qdist013.avtaltmelding.v1;

import jakarta.xml.bind.JAXBElement;
import no.arkivverket.standarder.noark5.arkivmelding.Arkivmelding;
import no.arkivverket.standarder.noark5.arkivmelding.Dokumentbeskrivelse;
import no.arkivverket.standarder.noark5.arkivmelding.Dokumentobjekt;
import no.arkivverket.standarder.noark5.arkivmelding.Journalpost;
import no.arkivverket.standarder.noark5.arkivmelding.Korrespondansepart;
import no.arkivverket.standarder.noark5.arkivmelding.Mappe;
import no.arkivverket.standarder.noark5.arkivmelding.Part;
import no.arkivverket.standarder.noark5.arkivmelding.Registrering;
import no.arkivverket.standarder.noark5.arkivmelding.Saksmappe;
import no.nav.dokdisteformidling.consumer.ereg.EregConsumer;
import no.nav.dokdisteformidling.consumer.pdl.HentPersonInfo;
import no.nav.dokdisteformidling.consumer.pdl.PdlGraphQLConsumer;
import no.nav.dokdisteformidling.consumer.saf.SafJournalpostQueryService;
import no.nav.dokdisteformidling.qdist013.saf.lightweight.LightweightSafJournalpostQdist013;
import no.nav.dokdisteformidling.qdist013.saf.main.JournalpostQdist013;
import no.nav.dokdisteformidling.qdist013.util.AvtaltmeldingConstant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;

import static java.util.Collections.singletonList;
import static no.nav.dokdisteformidling.constants.DomainConstants.APP_NAME;
import static no.nav.dokdisteformidling.constants.DomainConstants.VARIANTFORMAT_ARKIV;
import static no.nav.dokdisteformidling.constants.DomainConstants.VARIANTFORMAT_PRODUKSJON;
import static no.nav.dokdisteformidling.constants.DomainConstants.VARIANTFORMAT_SLADDET;
import static no.nav.dokdisteformidling.consumer.eformidling.EformidlingConstants.NAV_KLAGEINSTANS_STYRINGSENHETEN_ORGNUMMER;
import static no.nav.dokdisteformidling.consumer.eformidling.EformidlingConstants.TRYGDERETTEN_ORGNUMMER;
import static no.nav.dokdisteformidling.qdist013.TestUtil.convertFromXmlGregorianCalendarToLocalDateTime;
import static no.nav.dokdisteformidling.qdist013.avtaltmelding.v1.AvtaltmeldingV1Mapper.FERDIGSTILT;
import static no.nav.dokdisteformidling.qdist013.avtaltmelding.v1.AvtaltmeldingV1Mapper.INNGAAENDE;
import static no.nav.dokdisteformidling.qdist013.avtaltmelding.v1.AvtaltmeldingV1Mapper.NAV_KLAGEINSTANS;
import static no.nav.dokdisteformidling.qdist013.avtaltmelding.v1.AvtaltmeldingV1Mapper.SAKSPART_ROLLE_AMP;
import static no.nav.dokdisteformidling.qdist013.avtaltmelding.v1.AvtaltmeldingV1Mapper.SAKSPART_ROLLE_DAP;
import static no.nav.dokdisteformidling.qdist013.avtaltmelding.v1.AvtaltmeldingV1Mapper.TRYGDERETTEN;
import static no.nav.dokdisteformidling.qdist013.avtaltmelding.v1.AvtaltmeldingV1Mapper.UKJENT;
import static no.nav.dokdisteformidling.qdist013.avtaltmelding.v1.AvtaltmeldingV1Mapper.UTGAAENDE;
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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AvtaltmeldingV1MapperTest {

	static final String BESTILLINGS_ID = "bestillingsId";
	static final String JOURNALPOST_ID = "987654321";
	static final String ARKIV_SAKNUMMER = "111111";
	static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2025-03-15T12:15:30.01Z"), ZoneId.of("Europe/Oslo"));
	static final LocalDateTime FIXED_LOCAL_DATE_TIME = LocalDateTime.now(FIXED_CLOCK);
	static final LocalDateTime DATO_OPPRETTET_SAK = FIXED_LOCAL_DATE_TIME;
	static final LocalDateTime DATO_OPPRETTET_JOURNALPOST = FIXED_LOCAL_DATE_TIME.minusDays(1);
	static final LocalDateTime DATO_JOURNALFOERT = FIXED_LOCAL_DATE_TIME.minusDays(2);
	static final String OPPRETTET_AV_NAVN = "Sak Sakbehandlersen";
	static final String BRUKER_ID_FNR = "20026900000";
	static final String BRUKER_TYPE_FNR = "FNR";
	static final String BRUKER_ID_ORGNR = "999999999";
	static final String BRUKER_TYPE_ORGNR = "ORGNR";
	static final String BRUKER_ID_AKTOER_ID = "aktoerId";
	static final String BRUKER_TYPE_AKTOER_ID = "AKTOERID";
	static final String TITTEL = "Klage på saksbehandling";
	static final String JOURNALFOERT_AV_NAVN = "Sak Sakbehandlersen";
	static final String TEMA_NAVN = "Dagpenger";
	static final String TEMA = "DAG";

	static final String DOKUMENT_INFO_ID_HOVEDDOK = "1234567";
	static final String TITTEL_HOVEDDOK = "Klage på saksbehandling";

	static final String DOKUMENT_INFO_ID_VEDLEGG = "7654321";
	static final String TITTEL_VEDLEGG = "Dokumentasjon til klage";
	static final String ORIGINAL_JPID_VEDLEGG = "1111111111";

	static final String DOKUMENT_INFO_ID_VEDLEGG_2 = "9876543";
	static final String EREG_NAVN = "Bedrift AS";
	static final String PDL_NAVN = "Bjarne Betjent";

	static final String AVSENDER_MOTTAKER_NAVN_ORIG_JP = "avsenderMottakerNavnOrigJp";
	static final String JOURNALFOERT_AV_NAVN_ORIG_JP = "ajournalfoertAvNavnOrigJp";
	static final LocalDateTime DATO_JOURNALFOERT_ORIG_JP = FIXED_LOCAL_DATE_TIME.minusDays(5);

	static final String FILTYPE_PNG = "PNG";
	static final String FILTYPE_JPEG = "JPEG";
	static final String FILTYPE_PDF = "PDF";
	static final String FILTYPE_PDFA = "PDF/A";

	private EregConsumer eregMock;
	private SafJournalpostQueryService<LightweightSafJournalpostQdist013> safJournalpostQueryServiceMock;
	private AvtaltmeldingV1Mapper avtaltmeldingV1Mapper;
	private PdlGraphQLConsumer pdlGraphQLConsumer;

	@BeforeEach
	public void setUp() {
		eregMock = mock(EregConsumer.class);
		pdlGraphQLConsumer = mock(PdlGraphQLConsumer.class);
		safJournalpostQueryServiceMock = mock(SafJournalpostQueryService.class);
		avtaltmeldingV1Mapper = new AvtaltmeldingV1Mapper(safJournalpostQueryServiceMock, eregMock, pdlGraphQLConsumer);
	}

	@Test
	@DisplayName("Asserts all fields")
	void fullHappyPath() {
		when(pdlGraphQLConsumer.hentPerson(anyString())).thenReturn(creatHentPersonInfo());
		JAXBElement<Arkivmelding> arkivmeldingJAXBElement = avtaltmeldingV1Mapper.createArkivMelding(createJournalpostQdist013Builder()
				.tema(TEMA)
				.build(), BESTILLINGS_ID);

		assertThat(arkivmeldingJAXBElement, notNullValue());
		Arkivmelding arkivmelding = arkivmeldingJAXBElement.getValue();
		assertArkivmelding(arkivmelding);

		verify(pdlGraphQLConsumer, times(1)).hentPerson(anyString());
		verify(eregMock, times(0)).hentOrganisasjonsnavn(any(String.class));
	}

	@Test
	@DisplayName("Case when bruker is organisasjon. Should get name from Ereg")
	void happyPathBrukerIsOrganisasjon() {
		when(eregMock.hentOrganisasjonsnavn(any(String.class))).thenReturn(EREG_NAVN);

		JournalpostQdist013 journalpostQdist013 = createJournalpostQdist013Builder()
				.tema(TEMA)
				.bruker(JournalpostQdist013.Bruker.builder()
						.id(BRUKER_ID_ORGNR)
						.type(BRUKER_TYPE_ORGNR)
						.build())
				.build();

		JAXBElement<Arkivmelding> arkivmeldingJAXBElement = avtaltmeldingV1Mapper.createArkivMelding(journalpostQdist013, BESTILLINGS_ID);
		Arkivmelding arkivmelding = arkivmeldingJAXBElement.getValue();
		List<Mappe> mappeList = arkivmelding.getMappe();
		Saksmappe saksmappe = (Saksmappe) mappeList.getFirst();
		Part sakspartDAP = saksmappe.getPart().get(1);

		assertEquals(EREG_NAVN, sakspartDAP.getPartNavn());
		assertEquals(SAKSPART_ROLLE_DAP, sakspartDAP.getPartRolle());
		assertEquals(BRUKER_ID_ORGNR, sakspartDAP.getOrganisasjonsnummer().getOrganisasjonsnummer());
		assertNull(sakspartDAP.getKontaktperson());

		verify(eregMock, times(1)).hentOrganisasjonsnavn(BRUKER_ID_ORGNR);
		verify(safJournalpostQueryServiceMock, times(0)).hentJournalpost(any(String.class));
	}

	@Test
	@DisplayName("Case when bruker is aktoer. Should get fnr from aktoerregister")
	void happyPathBrukerIsAktoer() {
		when(pdlGraphQLConsumer.hentPerson(anyString())).thenReturn(creatHentPersonInfo());

		JournalpostQdist013 journalpostQdist013 = createJournalpostQdist013Builder()
				.tema(TEMA)
				.bruker(JournalpostQdist013.Bruker.builder()
						.id(BRUKER_ID_AKTOER_ID)
						.type(BRUKER_TYPE_AKTOER_ID)
						.build())
				.build();

		JAXBElement<Arkivmelding> arkivmeldingJAXBElement = avtaltmeldingV1Mapper.createArkivMelding(journalpostQdist013, BESTILLINGS_ID);
		Arkivmelding arkivmelding = arkivmeldingJAXBElement.getValue();
		List<Mappe> mappeList = arkivmelding.getMappe();
		Saksmappe saksmappe = (Saksmappe) mappeList.getFirst();
		Part sakspartDAP = saksmappe.getPart().get(1);

		assertEquals(PDL_NAVN, sakspartDAP.getPartNavn());
		assertEquals(SAKSPART_ROLLE_DAP, sakspartDAP.getPartRolle());
		assertNull(sakspartDAP.getKontaktperson());

		verify(pdlGraphQLConsumer, times(2)).hentPerson(anyString());
		verify(eregMock, times(0)).hentOrganisasjonsnavn(any(String.class));
		verify(safJournalpostQueryServiceMock, times(0)).hentJournalpost(any(String.class));
	}

	@Test
	@DisplayName("Case for satt originalJournalPostId men ukjent datoJournal")
	void shouldMapOpprettetDatoWhenNullDatoJournalISafJournalpostgetJournalfortAndVedleggHasOriginalJpId() {
		JournalpostQdist013 journalpostQdist013 = createJournalpostQdist013Builder()
				.tema(TEMA)
				.journalposttype(INNGAAENDE)
				.dokumenter(Arrays.asList(createHoveddokumentBuilder().build(),
						createVedleggBuilderUtenDato().build()))
				.build();
		when(pdlGraphQLConsumer.hentPerson(anyString())).thenReturn(creatHentPersonInfo());
		JAXBElement<Arkivmelding> arkivmeldingJAXBElement = avtaltmeldingV1Mapper.createArkivMelding(journalpostQdist013, BESTILLINGS_ID);
		Arkivmelding arkivmelding = arkivmeldingJAXBElement.getValue();
		List<Mappe> mappeList = arkivmelding.getMappe();
		Saksmappe saksmappe = (Saksmappe) mappeList.getFirst();
		Journalpost registreringJp = (Journalpost) saksmappe.getRegistrering().getFirst();
		Dokumentbeskrivelse dokumentbeskrivelse = registreringJp.getDokumentbeskrivelse()
				.get(1);

		assertEquals(TITTEL_VEDLEGG, dokumentbeskrivelse.getTittel());

		verify(safJournalpostQueryServiceMock, times(0)).hentJournalpost(ORIGINAL_JPID_VEDLEGG);
	}

	@Test
	@DisplayName("Case when journalposttype is inngaaende and vedlegg has original jpId. Should make a custom dokumenttittel")
	void happyPathTestTittelWhenJpIsInngaaendeAndVedleggHasOriginalJpId() {
		when(safJournalpostQueryServiceMock.hentJournalpost(ORIGINAL_JPID_VEDLEGG)).thenReturn(createLightweightSafJournalpostQdist013());
		when(pdlGraphQLConsumer.hentPerson(anyString())).thenReturn(creatHentPersonInfo());
		JournalpostQdist013 journalpostQdist013 = createJournalpostQdist013Builder()
				.tema(TEMA)
				.journalposttype(INNGAAENDE)
				.dokumenter(Arrays.asList(createHoveddokumentBuilder().build(),
						createVedleggBuilder().originalJournalpostId(ORIGINAL_JPID_VEDLEGG).build()))
				.build();

		JAXBElement<Arkivmelding> arkivmeldingJAXBElement = avtaltmeldingV1Mapper.createArkivMelding(journalpostQdist013, BESTILLINGS_ID);
		Arkivmelding arkivmelding = arkivmeldingJAXBElement.getValue();
		List<Mappe> mappeList = arkivmelding.getMappe();
		Saksmappe saksmappe = (Saksmappe) mappeList.getFirst();
		Journalpost registreringJp = (Journalpost) saksmappe.getRegistrering().getFirst();
		Dokumentbeskrivelse dokumentbeskrivelse = registreringJp.getDokumentbeskrivelse()
				.get(1);

		assertEquals(dokumentbeskrivelse.getTittel(), TITTEL_VEDLEGG + ", Til " + AVSENDER_MOTTAKER_NAVN_ORIG_JP);

		verify(safJournalpostQueryServiceMock, times(18)).hentJournalpost(ORIGINAL_JPID_VEDLEGG);
	}

	@Test
	@DisplayName("Case when journalposttype is utgaaende and vedlegg has original jpId. Should make a custom dokumenttittel")
	void happyPathTestTittelWhenJpIsUtgaaendeAndVedleggHasOriginalJpId() {
		when(safJournalpostQueryServiceMock.hentJournalpost(ORIGINAL_JPID_VEDLEGG)).thenReturn(createLightweightSafJournalpostQdist013());
		when(pdlGraphQLConsumer.hentPerson(anyString())).thenReturn(creatHentPersonInfo());
		JournalpostQdist013 journalpostQdist013 = createJournalpostQdist013Builder()
				.tema(TEMA)
				.journalposttype(UTGAAENDE)
				.dokumenter(Arrays.asList(createHoveddokumentBuilder().build(),
						createVedleggBuilder().originalJournalpostId(ORIGINAL_JPID_VEDLEGG).build()))
				.build();

		JAXBElement<Arkivmelding> arkivmeldingJAXBElement = avtaltmeldingV1Mapper.createArkivMelding(journalpostQdist013, BESTILLINGS_ID);
		Arkivmelding arkivmelding = arkivmeldingJAXBElement.getValue();
		List<Mappe> mappeList = arkivmelding.getMappe();
		Saksmappe saksmappe = (Saksmappe) mappeList.getFirst();
		Journalpost registreringJp = (Journalpost) saksmappe.getRegistrering().getFirst();
		Dokumentbeskrivelse dokumentbeskrivelse = registreringJp.getDokumentbeskrivelse()
				.get(1);

		assertEquals(dokumentbeskrivelse.getTittel(), TITTEL_VEDLEGG + ", Til " + AVSENDER_MOTTAKER_NAVN_ORIG_JP);

		verify(safJournalpostQueryServiceMock, times(18)).hentJournalpost(ORIGINAL_JPID_VEDLEGG);
	}

	@Test
	@DisplayName("Case when journalposttype is notat and vedlegg has original jpId. Should not make a custom dokumenttittel")
	void happyPathTestTittelWhenJpIsNotatAndVedleggHasOriginalJpId() {
		when(safJournalpostQueryServiceMock.hentJournalpost(ORIGINAL_JPID_VEDLEGG)).thenReturn(createLightweightSafJournalpostQdist013());
		when(pdlGraphQLConsumer.hentPerson(anyString())).thenReturn(creatHentPersonInfo());
		JournalpostQdist013 journalpostQdist013 = createJournalpostQdist013Builder()
				.tema(TEMA)
				.journalposttype("Notat")
				.dokumenter(Arrays.asList(createHoveddokumentBuilder().build(),
						createVedleggBuilder().originalJournalpostId(ORIGINAL_JPID_VEDLEGG).build()))
				.build();

		JAXBElement<Arkivmelding> arkivmeldingJAXBElement = avtaltmeldingV1Mapper.createArkivMelding(journalpostQdist013, BESTILLINGS_ID);
		Arkivmelding arkivmelding = arkivmeldingJAXBElement.getValue();
		List<Mappe> mappeList = arkivmelding.getMappe();
		Saksmappe saksmappe = (Saksmappe) mappeList.getFirst();
		Journalpost registreringJp = (Journalpost) saksmappe.getRegistrering().getFirst();
		Dokumentbeskrivelse dokumentbeskrivelse = registreringJp.getDokumentbeskrivelse()
				.get(1);

		assertEquals(dokumentbeskrivelse.getTittel(), TITTEL_VEDLEGG + ", Til " + AVSENDER_MOTTAKER_NAVN_ORIG_JP);

		verify(safJournalpostQueryServiceMock, times(18)).hentJournalpost(ORIGINAL_JPID_VEDLEGG);
	}

	@Test
	@DisplayName("Case when vedlegg has original jpId. Should get opprettet dato from original journalpost")
	void happyPathTestOpprettetDatoWhenVedleggHasOriginalJpId() {
		when(safJournalpostQueryServiceMock.hentJournalpost(ORIGINAL_JPID_VEDLEGG)).thenReturn(createLightweightSafJournalpostQdist013());
		when(pdlGraphQLConsumer.hentPerson(anyString())).thenReturn(creatHentPersonInfo());
		JournalpostQdist013 journalpostQdist013 = createJournalpostQdist013Builder()
				.tema(TEMA)
				.dokumenter(Arrays.asList(createHoveddokumentBuilder().build(),
						createVedleggBuilder().originalJournalpostId(ORIGINAL_JPID_VEDLEGG).build()))
				.build();

		JAXBElement<Arkivmelding> arkivmeldingJAXBElement = avtaltmeldingV1Mapper.createArkivMelding(journalpostQdist013, BESTILLINGS_ID);
		Arkivmelding arkivmelding = arkivmeldingJAXBElement.getValue();
		List<Mappe> mappeList = arkivmelding.getMappe();
		Saksmappe saksmappe = (Saksmappe) mappeList.getFirst();
		Journalpost registreringJp = (Journalpost) saksmappe.getRegistrering().getFirst();

		Dokumentbeskrivelse dokumentbeskrivelseHoveddok = registreringJp.getDokumentbeskrivelse()
				.get(0);
		Dokumentobjekt dokumentobjektHoveddok = dokumentbeskrivelseHoveddok.getDokumentobjekt().getFirst();
		Dokumentbeskrivelse dokumentbeskrivelseVedlegg = registreringJp.getDokumentbeskrivelse()
				.get(1);
		Dokumentobjekt dokumentobjektVedlegg = dokumentbeskrivelseVedlegg.getDokumentobjekt().getFirst();

		assertEquals(convertFromXmlGregorianCalendarToLocalDateTime(dokumentbeskrivelseHoveddok.getOpprettetDato()).toString(), DATO_JOURNALFOERT.toString());
		assertEquals(convertFromXmlGregorianCalendarToLocalDateTime(dokumentobjektHoveddok.getOpprettetDato()).toString(), DATO_JOURNALFOERT.toString());
		assertEquals(convertFromXmlGregorianCalendarToLocalDateTime(dokumentbeskrivelseVedlegg.getOpprettetDato()).toString(), DATO_JOURNALFOERT_ORIG_JP.toString());
		assertEquals(convertFromXmlGregorianCalendarToLocalDateTime(dokumentobjektVedlegg.getOpprettetDato()).toString(), DATO_JOURNALFOERT_ORIG_JP.toString());

		verify(safJournalpostQueryServiceMock, times(18)).hentJournalpost(ORIGINAL_JPID_VEDLEGG);
	}

	@Test
	@DisplayName("Case when vedlegg has original jpId. Should get opprettet av from original journalpost")
	void happyPathTestOpprettetAvWhenVedleggHasOriginalJpId() {
		when(safJournalpostQueryServiceMock.hentJournalpost(ORIGINAL_JPID_VEDLEGG)).thenReturn(createLightweightSafJournalpostQdist013());
		when(pdlGraphQLConsumer.hentPerson(anyString())).thenReturn(creatHentPersonInfo());
		JournalpostQdist013 journalpostQdist013 = createJournalpostQdist013Builder()
				.tema(TEMA)
				.dokumenter(Arrays.asList(createHoveddokumentBuilder().build(),
						createVedleggBuilder().originalJournalpostId(ORIGINAL_JPID_VEDLEGG).build()))
				.build();

		JAXBElement<Arkivmelding> arkivmeldingJAXBElement = avtaltmeldingV1Mapper.createArkivMelding(journalpostQdist013, BESTILLINGS_ID);
		Arkivmelding arkivmelding = arkivmeldingJAXBElement.getValue();
		List<Mappe> mappeList = arkivmelding.getMappe();
		Saksmappe saksmappe = (Saksmappe) mappeList.getFirst();
		Journalpost registreringJp = (Journalpost) saksmappe.getRegistrering().getFirst();

		Dokumentbeskrivelse dokumentbeskrivelseHoveddok = registreringJp.getDokumentbeskrivelse()
				.get(0);
		Dokumentobjekt dokumentobjektHoveddok = dokumentbeskrivelseHoveddok.getDokumentobjekt().getFirst();
		Dokumentbeskrivelse dokumentbeskrivelseVedlegg = registreringJp.getDokumentbeskrivelse()
				.get(1);
		Dokumentobjekt dokumentobjektVedlegg = dokumentbeskrivelseVedlegg.getDokumentobjekt().getFirst();

		assertEquals(JOURNALFOERT_AV_NAVN, dokumentbeskrivelseHoveddok.getOpprettetAv());
		assertEquals(JOURNALFOERT_AV_NAVN, dokumentobjektHoveddok.getOpprettetAv());
		assertEquals(JOURNALFOERT_AV_NAVN_ORIG_JP, dokumentbeskrivelseVedlegg.getOpprettetAv());
		assertEquals(JOURNALFOERT_AV_NAVN_ORIG_JP, dokumentobjektVedlegg.getOpprettetAv());

		verify(safJournalpostQueryServiceMock, times(18)).hentJournalpost(ORIGINAL_JPID_VEDLEGG);
	}

	@Test
	@DisplayName("Case when dokument does not have variantformat SLADDET (variantformat is ARKIV) and filtype is not PNG or JPEG. Should set variantformat to Produksjonsformat")
	void happyPathTestNoSladdetVariantAndFiltypeNotPngOrJPEG() {
		when(safJournalpostQueryServiceMock.hentJournalpost(ORIGINAL_JPID_VEDLEGG)).thenReturn(createLightweightSafJournalpostQdist013());
		when(pdlGraphQLConsumer.hentPerson(anyString())).thenReturn(creatHentPersonInfo());
		JournalpostQdist013 journalpostQdist013 = createJournalpostQdist013Builder()
				.tema(TEMA)
				.dokumenter(singletonList(createHoveddokumentBuilder()
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

		JAXBElement<Arkivmelding> arkivmeldingJAXBElement = avtaltmeldingV1Mapper.createArkivMelding(journalpostQdist013, BESTILLINGS_ID);
		Arkivmelding arkivmelding = arkivmeldingJAXBElement.getValue();
		List<Mappe> mappeList = arkivmelding.getMappe();
		Saksmappe saksmappe = (Saksmappe) mappeList.getFirst();
		Journalpost registreringJp = (Journalpost) saksmappe.getRegistrering().getFirst();

		Dokumentbeskrivelse dokumentbeskrivelseHoveddok = registreringJp.getDokumentbeskrivelse()
				.getFirst();
		Dokumentobjekt dokumentobjektHoveddok = dokumentbeskrivelseHoveddok.getDokumentobjekt().getFirst();

		assertEquals(PRODUKSJONSFORMAT, dokumentobjektHoveddok.getVariantformat());
		assertTrue(dokumentobjektHoveddok.getReferanseDokumentfil().contains(PRODUKSJONSFORMAT));
	}

	@Test
	@DisplayName("Case when dokument does not have variantformat SLADDET (variantformat is ARKIV) and filtype is PNG. Should set variantformat to Arkivformat")
	void happyPathTestNoSladdetVariantAndFiltypeIsPng() {
		when(safJournalpostQueryServiceMock.hentJournalpost(ORIGINAL_JPID_VEDLEGG)).thenReturn(createLightweightSafJournalpostQdist013());
		when(pdlGraphQLConsumer.hentPerson(anyString())).thenReturn(creatHentPersonInfo());
		JournalpostQdist013 journalpostQdist013 = createJournalpostQdist013Builder()
				.tema(TEMA)
				.dokumenter(singletonList(createHoveddokumentBuilder()
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

		JAXBElement<Arkivmelding> arkivmeldingJAXBElement = avtaltmeldingV1Mapper.createArkivMelding(journalpostQdist013, BESTILLINGS_ID);
		Arkivmelding arkivmelding = arkivmeldingJAXBElement.getValue();
		List<Mappe> mappeList = arkivmelding.getMappe();
		Saksmappe saksmappe = (Saksmappe) mappeList.getFirst();
		Journalpost registreringJp = (Journalpost) saksmappe.getRegistrering().getFirst();

		Dokumentbeskrivelse dokumentbeskrivelseHoveddok = registreringJp.getDokumentbeskrivelse()
				.getFirst();
		Dokumentobjekt dokumentobjektHoveddok = dokumentbeskrivelseHoveddok.getDokumentobjekt().getFirst();

		assertEquals(ARKIVFORMAT, dokumentobjektHoveddok.getVariantformat());
		assertTrue(dokumentobjektHoveddok.getReferanseDokumentfil().contains(ARKIVFORMAT));
	}

	@Test
	@DisplayName("Case when dokument does not have variantformat SLADDET (variantformat is ARKIV) and filtype is JPEG. Should set variantformat to Arkivformat")
	void happyPathTestNoSladdetVariantAndFiltypeIsJPEG() {
		when(safJournalpostQueryServiceMock.hentJournalpost(ORIGINAL_JPID_VEDLEGG)).thenReturn(createLightweightSafJournalpostQdist013());
		when(pdlGraphQLConsumer.hentPerson(anyString())).thenReturn(creatHentPersonInfo());
		JournalpostQdist013 journalpostQdist013 = createJournalpostQdist013Builder()
				.tema(TEMA)
				.dokumenter(singletonList(createHoveddokumentBuilder()
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

		JAXBElement<Arkivmelding> arkivmeldingJAXBElement = avtaltmeldingV1Mapper.createArkivMelding(journalpostQdist013, BESTILLINGS_ID);
		Arkivmelding arkivmelding = arkivmeldingJAXBElement.getValue();
		List<Mappe> mappeList = arkivmelding.getMappe();
		Saksmappe saksmappe = (Saksmappe) mappeList.getFirst();
		Journalpost registreringJp = (Journalpost) saksmappe.getRegistrering().getFirst();

		Dokumentbeskrivelse dokumentbeskrivelseHoveddok = registreringJp.getDokumentbeskrivelse()
				.getFirst();
		Dokumentobjekt dokumentobjektHoveddok = dokumentbeskrivelseHoveddok.getDokumentobjekt().getFirst();

		assertEquals(ARKIVFORMAT, dokumentobjektHoveddok.getVariantformat());
		assertTrue(dokumentobjektHoveddok.getReferanseDokumentfil().contains(ARKIVFORMAT));
	}

	@Test
	@DisplayName("Case when vedlegg has no dokumentstatus set. That vedlegg should be considered FERDIGSTILT.")
	void happyPathVedleggFerdigstiltUtenStatus() {
		when(pdlGraphQLConsumer.hentPerson(anyString())).thenReturn(creatHentPersonInfo());
		JournalpostQdist013 journalpostQdist013 = createJournalpostQdist013Builder()
				.tema(TEMA)
				.dokumenter(Arrays.asList(createHoveddokumentBuilder().build(),
						createVedleggBuilder().dokumentstatus(null).build()))
				.build();

		JAXBElement<Arkivmelding> arkivmeldingJAXBElement = avtaltmeldingV1Mapper.createArkivMelding(journalpostQdist013, BESTILLINGS_ID);
		Arkivmelding arkivmelding = arkivmeldingJAXBElement.getValue();
		List<Mappe> mappeList = arkivmelding.getMappe();
		Saksmappe saksmappe = (Saksmappe) mappeList.getFirst();
		Journalpost registreringJp = (Journalpost) saksmappe.getRegistrering().getFirst();

		assertEquals(2, arkivmelding.getAntallFiler());
		assertDokumentbeskrivelseOpprettetAv(registreringJp.getDokumentbeskrivelse());
	}

	@Test
	@DisplayName("Case when vedlegg does not have dokumentstatus FERDIGSTILT. That vedlegg should not be mapped.")
	void happyPathIkkeFerdigstiltVedlegg() {
		when(pdlGraphQLConsumer.hentPerson(anyString())).thenReturn(creatHentPersonInfo());
		when(safJournalpostQueryServiceMock.hentJournalpost(ORIGINAL_JPID_VEDLEGG)).thenReturn(createLightweightSafJournalpostQdist013());
		JournalpostQdist013 journalpostQdist013 = createJournalpostQdist013Builder()
				.tema(TEMA)
				.dokumenter(Arrays.asList(createHoveddokumentBuilder().build(),
						createVedleggBuilder().dokumentInfoId(DOKUMENT_INFO_ID_VEDLEGG_2)
								.dokumentstatus("UNDER_REDIGERING")
								.build(),
						createVedleggBuilder().build()))
				.build();

		JAXBElement<Arkivmelding> arkivmeldingJAXBElement = avtaltmeldingV1Mapper.createArkivMelding(journalpostQdist013, BESTILLINGS_ID);
		Arkivmelding arkivmelding = arkivmeldingJAXBElement.getValue();
		List<Mappe> mappeList = arkivmelding.getMappe();
		Saksmappe saksmappe = (Saksmappe) mappeList.getFirst();
		Journalpost registreringJp = (Journalpost) saksmappe.getRegistrering().getFirst();

		assertEquals(2, arkivmelding.getAntallFiler());
		assertDokumentbeskrivelseOpprettetAv(registreringJp.getDokumentbeskrivelse());
	}

	@Test
	@DisplayName("Case for satt originalJournalPostId men ukjent journalfører")
	void assertUkjentVedsafJournalpostgetJournalfortAvNavnErNull() {
		when(safJournalpostQueryServiceMock.hentJournalpost(ORIGINAL_JPID_VEDLEGG)).thenReturn(createLightweightSafJournalpostQdist013NoJournalFoertAv());
		when(pdlGraphQLConsumer.hentPerson(anyString())).thenReturn(creatHentPersonInfo());
		JournalpostQdist013 journalpostQdist013 = createJournalpostQdist013BuilderNoJournalFoertAv()
				.tema(TEMA)
				.dokumenter(Arrays.asList(createHoveddokumentBuilder().build(),
						createVedleggBuilder().originalJournalpostId(ORIGINAL_JPID_VEDLEGG).build()))
				.build();

		JAXBElement<Arkivmelding> arkivmeldingJAXBElement = avtaltmeldingV1Mapper.createArkivMelding(journalpostQdist013, BESTILLINGS_ID);
		Arkivmelding arkivmelding = arkivmeldingJAXBElement.getValue();
		List<Mappe> mappeList = arkivmelding.getMappe();
		Saksmappe saksmappe = (Saksmappe) mappeList.getFirst();
		Journalpost registreringJp = (Journalpost) saksmappe.getRegistrering().getFirst();

		Dokumentbeskrivelse dokumentBeskrivelseVedlegg = registreringJp.getDokumentbeskrivelse()
				.get(1);
		Dokumentobjekt dokumentVedlegg = dokumentBeskrivelseVedlegg.getDokumentobjekt().getFirst();

		assertEquals(UKJENT, dokumentVedlegg.getOpprettetAv());
	}

	@Test
	@DisplayName("Når sak mangler opprettetDato, sett opprettetDato fra eldste vedlegg sortert etter journalpostens dokumentbeskrivelse.opprettetDato")
	void shouldSetteOpprettetDatoPaaSakFraJournalpostTilhorendeTilEldsteVedlegg() {
		LocalDateTime femDagerSiden = FIXED_LOCAL_DATE_TIME.minusDays(5);
		LocalDateTime treDagerSiden = FIXED_LOCAL_DATE_TIME.minusDays(3);
		when(pdlGraphQLConsumer.hentPerson(anyString())).thenReturn(creatHentPersonInfo());
		when(safJournalpostQueryServiceMock
				.hentJournalpost(DOKUMENT_INFO_ID_VEDLEGG))
				.thenReturn(createLightweightSafJournalpostQdist013Builder()
						.datoJournalfoert(treDagerSiden)
						.build()
				);
		when(safJournalpostQueryServiceMock
				.hentJournalpost(DOKUMENT_INFO_ID_VEDLEGG_2))
				.thenReturn(createLightweightSafJournalpostQdist013Builder()
						.datoJournalfoert(femDagerSiden)
						.build()
				);

		JournalpostQdist013.Sak sakUtenOpprettetDato = JournalpostQdist013.Sak.builder()
				.arkivsaksnummer(ARKIV_SAKNUMMER)
				.build();
		JAXBElement<Arkivmelding> arkivmeldingJAXBElement = avtaltmeldingV1Mapper.createArkivMelding(createJournalpostQdist013Builder()
				.sak(sakUtenOpprettetDato)
				.dokumenter(Arrays.asList(createHoveddokumentBuilder().build(),
						createVedleggBuilder().originalJournalpostId(DOKUMENT_INFO_ID_VEDLEGG).build(),
						createVedleggBuilder().originalJournalpostId(DOKUMENT_INFO_ID_VEDLEGG_2).build()))
				.build(), BESTILLINGS_ID);

		assertThat(arkivmeldingJAXBElement, notNullValue());
		Arkivmelding arkivmelding = arkivmeldingJAXBElement.getValue();

		assertEquals(femDagerSiden.toString(),
				convertFromXmlGregorianCalendarToLocalDateTime(arkivmelding.getMappe().getFirst().getOpprettetDato()).toString());
	}

	private void assertArkivmelding(Arkivmelding arkivmelding) {
		assertNotNull(arkivmelding);
		assertEquals(APP_NAME, arkivmelding.getSystem());
		assertEquals(BESTILLINGS_ID, arkivmelding.getMeldingId());
		assertNotNull(arkivmelding.getTidspunkt());
		assertEquals(2, arkivmelding.getAntallFiler());
		assertMappe(arkivmelding.getMappe());
	}

	private void assertMappe(List<Mappe> mappeList) {
		assertTrue(mappeList != null && mappeList.size() == 1);
		assertInstanceOf(Saksmappe.class, mappeList.getFirst());
		Saksmappe saksmappe = (Saksmappe) mappeList.getFirst();

		assertEquals(TEMA_NAVN, saksmappe.getTittel());
		assertEquals(convertFromXmlGregorianCalendarToLocalDateTime(saksmappe.getOpprettetDato()).toString(), DATO_OPPRETTET_SAK.toString());
		assertEquals(OPPRETTET_AV_NAVN, saksmappe.getOpprettetAv());
		assertEquals(ARKIV_SAKNUMMER, saksmappe.getVirksomhetsspesifikkeMetadata());
		assertRegistrering(saksmappe.getRegistrering());
		assertEquals(convertFromXmlGregorianCalendarToLocalDateTime(saksmappe.getSaksdato()).toString(), DATO_OPPRETTET_SAK.toString());
		assertEquals(NAV_KLAGEINSTANS, saksmappe.getAdministrativEnhet());
		assertEquals(OPPRETTET_AV_NAVN, saksmappe.getSaksansvarlig());
		assertEquals(UNDER_BEHANDLING, saksmappe.getSaksstatus());
		assertSakspart(saksmappe.getPart());
	}

	private void assertRegistrering(List<Registrering> registreringList) {
		assertTrue(registreringList != null && registreringList.size() == 1);
		assertInstanceOf(Journalpost.class, registreringList.getFirst());

		Journalpost registreringJp = (Journalpost) registreringList.getFirst();
		assertEquals(convertFromXmlGregorianCalendarToLocalDateTime(registreringJp.getOpprettetDato()).toString(), DATO_OPPRETTET_JOURNALPOST.toString());
		assertEquals(OPPRETTET_AV_NAVN, registreringJp.getOpprettetAv());
		assertDokumentbeskrivelseOpprettetAv(registreringJp.getDokumentbeskrivelse());
		assertEquals(TITTEL, registreringJp.getTittel());
		assertEquals(UTGAAENDE_DOKUMENT, registreringJp.getJournalposttype());
		assertEquals(EKSPEDERT, registreringJp.getJournalstatus());
		assertEquals(convertFromXmlGregorianCalendarToLocalDateTime(registreringJp.getJournaldato()).toString(), DATO_JOURNALFOERT.toString());
		assertKorrespondanseparter(registreringJp.getKorrespondansepart());
	}

	private void assertKorrespondanseparter(List<Korrespondansepart> korrespondansepartList) {
		assertTrue(korrespondansepartList != null && korrespondansepartList.size() == 2);

		Korrespondansepart mottaker = korrespondansepartList.getFirst();
		assertEquals(MOTTAKER, mottaker.getKorrespondanseparttype());
		assertEquals(TRYGDERETTEN, mottaker.getKorrespondansepartNavn());
		assertEquals(TRYGDERETTEN_ORGNUMMER, mottaker.getOrganisasjonsnummer().getOrganisasjonsnummer());

		Korrespondansepart avsender = korrespondansepartList.get(1);
		assertEquals(AVSENDER, avsender.getKorrespondanseparttype());
		assertEquals(NAV_KLAGEINSTANS, avsender.getKorrespondansepartNavn());
		assertEquals(NAV_KLAGEINSTANS_STYRINGSENHETEN_ORGNUMMER, avsender.getOrganisasjonsnummer().getOrganisasjonsnummer());
	}

	private void assertSakspart(List<Part> sakspartList) {
		assertTrue(sakspartList != null && sakspartList.size() == 2);

		Part sakspartAMP = sakspartList.getFirst();
		assertNull(sakspartAMP.getPartID());
		assertEquals(NAV_KLAGEINSTANS, sakspartAMP.getPartNavn());
		assertEquals(SAKSPART_ROLLE_AMP, sakspartAMP.getPartRolle());
		assertEquals(OPPRETTET_AV_NAVN, sakspartAMP.getKontaktperson());
		assertEquals(NAV_KLAGEINSTANS_STYRINGSENHETEN_ORGNUMMER, sakspartAMP.getOrganisasjonsnummer().getOrganisasjonsnummer());
		assertNull(sakspartAMP.getFoedselsnummer());
		assertNull(sakspartAMP.getDNummer());

		Part sakspartDAP = sakspartList.get(1);
		assertEquals(PDL_NAVN, sakspartDAP.getPartNavn());
		assertEquals(SAKSPART_ROLLE_DAP, sakspartDAP.getPartRolle());
		assertNull(sakspartDAP.getKontaktperson());
		assertEquals(BRUKER_ID_FNR, sakspartDAP.getFoedselsnummer().getFoedselsnummer());
	}

	private void assertDokumentbeskrivelseOpprettetAv(List<Dokumentbeskrivelse> dokumentbeskrivelseList) {
		assertTrue(dokumentbeskrivelseList != null && dokumentbeskrivelseList.size() == 2);

		assertNotNull(dokumentbeskrivelseList.getFirst());
		Dokumentbeskrivelse dokumentbeskrivelseHoveddok = dokumentbeskrivelseList.getFirst();
		assertEquals(AvtaltmeldingConstant.HOVEDDOKUMENT, dokumentbeskrivelseHoveddok.getTilknyttetRegistreringSom());
		assertEquals(BigInteger.ONE, dokumentbeskrivelseHoveddok.getDokumentnummer());
		assertCommonAttributesVedleggDokumentbeskrivelseOpprettetAv(dokumentbeskrivelseHoveddok);
		assertEquals(TITTEL_HOVEDDOK, dokumentbeskrivelseHoveddok.getTittel());
		assertEquals(convertFromXmlGregorianCalendarToLocalDateTime(dokumentbeskrivelseHoveddok.getOpprettetDato()).toString(), DATO_JOURNALFOERT.toString());

		assertDokumentobjektHoveddokument(dokumentbeskrivelseHoveddok.getDokumentobjekt());

		assertNotNull(dokumentbeskrivelseList.get(1));
		Dokumentbeskrivelse dokumentbeskrivelseVedlegg = dokumentbeskrivelseList.get(1);
		assertEquals(VEDLEGG, dokumentbeskrivelseVedlegg.getTilknyttetRegistreringSom());
		assertEquals(dokumentbeskrivelseVedlegg.getDokumentnummer(), BigInteger.valueOf(2));
		assertCommonAttributesVedleggDokumentbeskrivelseOpprettetAv(dokumentbeskrivelseVedlegg);
		assertEquals(TITTEL_VEDLEGG, dokumentbeskrivelseVedlegg.getTittel());
		assertEquals(convertFromXmlGregorianCalendarToLocalDateTime(dokumentbeskrivelseVedlegg.getOpprettetDato()).toString(), DATO_JOURNALFOERT.toString());

		assertDokumentobjektVedlegg(dokumentbeskrivelseVedlegg.getDokumentobjekt());
	}

	private void assertDokumentobjektHoveddokument(List<Dokumentobjekt> dokumentobjektList) {
		assertTrue(dokumentobjektList != null && dokumentobjektList.size() == 1);
		Dokumentobjekt dokumentobjektHoveddok = dokumentobjektList.getFirst();
		assertEquals(BigInteger.ONE, dokumentobjektHoveddok.getVersjonsnummer());
		assertEquals(DOKUMENT_HVOR_DELER_AV_INNHOLDET_ER_SKJERMET, dokumentobjektHoveddok.getVariantformat());
		assertEquals(convertFromXmlGregorianCalendarToLocalDateTime(dokumentobjektHoveddok.getOpprettetDato()).toString(), DATO_JOURNALFOERT.toString());
		assertEquals(JOURNALFOERT_AV_NAVN, dokumentobjektHoveddok.getOpprettetAv());
		assertEquals(dokumentobjektHoveddok.getReferanseDokumentfil(), JOURNALPOST_ID + "-" + DOKUMENT_INFO_ID_HOVEDDOK + "-" + DOKUMENT_HVOR_DELER_AV_INNHOLDET_ER_SKJERMET + "-" + FILTYPE_PDF);
	}

	private void assertDokumentobjektVedlegg(List<Dokumentobjekt> dokumentobjektList) {
		assertTrue(dokumentobjektList != null && dokumentobjektList.size() == 1);
		Dokumentobjekt dokumentobjektVedlegg = dokumentobjektList.getFirst();
		assertEquals(BigInteger.ONE, dokumentobjektVedlegg.getVersjonsnummer());
		assertEquals(ARKIVFORMAT, dokumentobjektVedlegg.getVariantformat());
		assertEquals(convertFromXmlGregorianCalendarToLocalDateTime(dokumentobjektVedlegg.getOpprettetDato()).toString(), DATO_JOURNALFOERT.toString());
		assertEquals(JOURNALFOERT_AV_NAVN, dokumentobjektVedlegg.getOpprettetAv());
		assertEquals(dokumentobjektVedlegg.getReferanseDokumentfil(), JOURNALPOST_ID + "-" + DOKUMENT_INFO_ID_VEDLEGG + "-" + ARKIVFORMAT + "-" + FILTYPE_JPEG);
	}

	private void assertCommonAttributesVedleggDokumentbeskrivelseOpprettetAv(Dokumentbeskrivelse dokumentbeskrivelse) {
		assertEquals(DOKUMENTASJON, dokumentbeskrivelse.getDokumenttype());
		assertEquals(DOKUMENTET_ER_FERDIGSTILT, dokumentbeskrivelse.getDokumentstatus());
		assertEquals(JOURNALFOERT_AV_NAVN, dokumentbeskrivelse.getOpprettetAv());
		assertNotNull(dokumentbeskrivelse.getTilknyttetDato());
		assertEquals(JOURNALFOERT_AV_NAVN, dokumentbeskrivelse.getTilknyttetAv());
	}

	static JournalpostQdist013.JournalpostQdist013Builder createJournalpostQdist013Builder() {
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
				.relevanteDatoer(singletonList(JournalpostQdist013.RelevantDato.builder()
						.datotype(JournalpostQdist013.Datotype.DATO_JOURNALFOERT)
						.dato(DATO_JOURNALFOERT)
						.build()))
				.dokumenter(Arrays.asList(createHoveddokumentBuilder().build(), createVedleggBuilder().build()
				));
	}

	static JournalpostQdist013.JournalpostQdist013Builder createJournalpostQdist013BuilderNoJournalFoertAv() {
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
				.relevanteDatoer(singletonList(JournalpostQdist013.RelevantDato.builder()
						.datotype(JournalpostQdist013.Datotype.DATO_JOURNALFOERT)
						.dato(DATO_JOURNALFOERT)
						.build()))
				.dokumenter(Arrays.asList(createHoveddokumentBuilder().build(), createVedleggBuilder().build()
				));
	}

	static JournalpostQdist013.DokumentInfo.DokumentInfoBuilder createHoveddokumentBuilder() {
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

	static JournalpostQdist013.DokumentInfo.DokumentInfoBuilder createVedleggBuilderUtenDato() {
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

	static JournalpostQdist013.DokumentInfo.DokumentInfoBuilder createVedleggBuilder() {
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

	static HentPersonInfo creatHentPersonInfo() {
		return HentPersonInfo.builder()
				.ident(BRUKER_ID_FNR)
				.fulltnavn(PDL_NAVN)
				.build();
	}

	static LightweightSafJournalpostQdist013 createLightweightSafJournalpostQdist013() {
		return LightweightSafJournalpostQdist013.builder()
				.avsenderMottakerNavn(AVSENDER_MOTTAKER_NAVN_ORIG_JP)
				.journalposttype(UTGAAENDE)
				.journalfortAvNavn(JOURNALFOERT_AV_NAVN_ORIG_JP)
				.datoJournalfoert(DATO_JOURNALFOERT_ORIG_JP)
				.build();
	}

	static LightweightSafJournalpostQdist013 createLightweightSafJournalpostQdist013NoJournalFoertAv() {
		return LightweightSafJournalpostQdist013.builder()
				.avsenderMottakerNavn(AVSENDER_MOTTAKER_NAVN_ORIG_JP)
				.datoJournalfoert(DATO_JOURNALFOERT_ORIG_JP)
				.build();
	}

	static LightweightSafJournalpostQdist013.LightweightSafJournalpostQdist013Builder createLightweightSafJournalpostQdist013Builder() {
		return LightweightSafJournalpostQdist013.builder()
				.avsenderMottakerNavn(AVSENDER_MOTTAKER_NAVN_ORIG_JP)
				.journalposttype(UTGAAENDE)
				.journalfortAvNavn(JOURNALFOERT_AV_NAVN_ORIG_JP)
				.datoJournalfoert(DATO_JOURNALFOERT_ORIG_JP);
	}

}