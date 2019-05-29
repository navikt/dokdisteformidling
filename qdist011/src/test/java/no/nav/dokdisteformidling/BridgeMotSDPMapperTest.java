package no.nav.dokdisteformidling;

import static no.nav.dokdisteformidling.qdist011.Qdist011FunctionalUtils.getNow;
import static no.nav.dokdisteformidling.qdist011.constants.BridgeMotSDPMapperConstants.AUTHORITY;
import static no.nav.dokdisteformidling.qdist011.constants.BridgeMotSDPMapperConstants.AUTHORITY_ENUM;
import static no.nav.dokdisteformidling.qdist011.constants.BridgeMotSDPMapperConstants.BUSINESS_SCOPE_TYPE;
import static no.nav.dokdisteformidling.qdist011.constants.BridgeMotSDPMapperConstants.DIGITAL_POST;
import static no.nav.dokdisteformidling.qdist011.constants.BridgeMotSDPMapperConstants.DOKUMENT_MIME;
import static no.nav.dokdisteformidling.qdist011.constants.BridgeMotSDPMapperConstants.ORGNR_NAV;
import static no.nav.dokdisteformidling.qdist011.constants.BridgeMotSDPMapperConstants.ORG_PREFIX;
import static no.nav.dokdisteformidling.qdist011.constants.BridgeMotSDPMapperConstants.SPRAAK_KODE;
import static no.nav.dokdisteformidling.qdist011.constants.BridgeMotSDPMapperConstants.STANDARD;
import static no.nav.dokdisteformidling.qdist011.constants.BridgeMotSDPMapperConstants.VERSION;
import static no.nav.dokdisteformidling.testUtils.getDateOnly;
import static no.nav.dokdisteformidling.testUtils.makePreferertKanalSet;
import static no.nav.dokdisteformidling.testUtils.makeUgyldigDate;
import static no.nav.dokdisteformidling.testUtils.varslingsTekster;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import no.difi.begrep.sdp.schema_v10.DigitalPost;
import no.difi.begrep.sdp.schema_v10.DigitalPostInfo;
import no.difi.begrep.sdp.schema_v10.Manifest;
import no.difi.begrep.sdp.schema_v10.Varsler;
import no.nav.dokdisteformidling.consumer.dki.HentSikkerDigitalPostadresseResponseTo;
import no.nav.dokdisteformidling.consumer.dokkat.tkat020.DokumenttypeInfoTo;
import no.nav.dokdisteformidling.consumer.dokkat.tkat021.VarselInfoTo;
import no.nav.dokdisteformidling.consumer.rdist001.HentForsendelseResponseTo;
import no.nav.dokdisteformidling.consumer.saf.journalpost.SafJournalpostTo;
import no.nav.dokdisteformidling.qdist011.BridgeMotSDPMapper;
import no.nav.tjeneste.virksomhet.digitalpost.senddigitalpost.v1.SendDigitalPost;
import no.nav.tjeneste.virksomhet.digitalpost.senddigitalpost.v1.meldinger.SendDigitalPostRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.unece.cefact.namespaces.standardbusinessdocumentheader.DocumentIdentification;
import org.unece.cefact.namespaces.standardbusinessdocumentheader.Scope;
import org.unece.cefact.namespaces.standardbusinessdocumentheader.StandardBusinessDocument;
import org.unece.cefact.namespaces.standardbusinessdocumentheader.StandardBusinessDocumentHeader;

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Arrays;
import java.util.List;

/**
 * @author Heidi Elisabeth Sando, Visma Consulting.
 */


public class BridgeMotSDPMapperTest {

	private static final String BESTILLINGS_ID = "bestillingsId";
	private static final String MODUS = "T";
	private static final String FORSENDELSE_STATUS = "forsendelseStatus";
	private static final String TEMA = "tema";
	private static final String FORSENDELSE_TITTEL = "forsendelseTittel";
	private static final String MOTTAKER_ID = "mottakerId";
	private static final String MOTTAKER_NAVN = "mottakerNavn";
	private static final String MOTTAKER_TYPE = "mottakerType";
	private static final String ARKIV_ID = "arkivId";
	private static final String ADRESSELINJE_1 = "adresselinje1";
	private static final String ADRESSELINJE_2 = "adresselinje2";
	private static final String ADRESSELINJE_3 = "adresselinje3";
	private static final String POSTNUMMER = "postnummer";
	private static final String POSTSTED = "poststed";
	private static final String LAND_KODE = "landKode";
	private static final String TILKNYTTET_SOM_HOVEDDOK = "HOVEDDOKUMENT";
	private static final String TILKNYTTET_SOM_VEDLEGG = "VEDLEGG";
	private static final String ARKIV_DOKUMENTINFO_ID_1 = "arkivDokumentinfoId1";
	private static final String ARKIV_DOKUMENTINFO_ID_2 = "arkivDokumentinfoId2";
	private static final String ARKIV_DOKUMENTINFO_ID_3 = "arkivDokumentinfoId3";
	private static final String DOKUMENTTYPE_ID_1 = "dokumenttypeId1";
	private static final String DOKUMENTTYPE_ID_2 = "dokumenttypeId2";
	private static final String DOKUMENTTYPE_ID_3 = "dokumenttypeId3";
	private static final String DOKUMENT_OBJEKT_REFERANSE_1 = "objektReferanse1";
	private static final String DOKUMENT_OBJEKT_REFERANSE_2 = "objektReferanse2";
	private static final String DOKUMENT_OBJEKT_REFERANSE_3 = "objektReferanse3";
	private static final byte[] SERTIFIKAT = "testSertifikat".getBytes();
	private static final String PERSONIDENT = "personident";
	private static final String INGEN_RESERVASJON = "NEI";
	private static final String EPOST_VALUE = "epostValue";
	private static final XMLGregorianCalendar GYLDIG_SIST_VERIFISERT = getNow();
	private static final XMLGregorianCalendar GYLDIG_SIST_OPPDATERT = getNow();
	private static final XMLGregorianCalendar UGYLDIG_SIST_VERIFISERT = makeUgyldigDate();
	private static final XMLGregorianCalendar UGYLDIG_SIST_OPPDATERT = makeUgyldigDate();
	private static final String MOBIL_VALUE = "mobilValue";
	private static final String LEVERANDOER_ADRESSE = ORG_PREFIX + "leverandoerAdresse";
	private static final String BRUKER_ADRESSE = "brukerAdresse";
	private static final String VARSEL_TYPE_ID = "varselTypeId";
	private static final int SIKKERHETSNIVAA = 0;
	private static final boolean STOPP_REPETERENDE_VARSEL = false;
	private static final String EPOST_VARSLINGS_TEKST = "epostVarslingsTekst";
	private static final String SMS_VARSLINGS_TEKST = "smsVarslingsTekst";
	private static final List<Integer> ANTALL_DAGER_LISTE = Arrays.asList(1, 2, 3);
	private static final String PREFERERT_KANAL_SMS = "SMS";
	private static final String PREFERERT_KANAL_EPOST = "EPOST";
	private static final String TITTEL_VEDLEGG_1 = "tittelVedlegg1";
	private static final String TITTEL_VEDLEGG_2 = "tittelVedlegg2";
	private static final boolean ER_PRIORITERT = false;

	private final BridgeMotSDPMapper bridgeMotSDPMapper = new BridgeMotSDPMapper();

	@Test
	public void shouldMap() {
		SendDigitalPost sendDigitalPost = bridgeMotSDPMapper.map(createHentForsendelseResponsToBuilder().build(),
				createHentSikkerDigitalPostadresseResponseToBuilder().build(), createDokumenttypeInfoTo(),
				createVarselInfoToBuilder().build(), createSafJournalpostTo());

		assertResponse(sendDigitalPost);
	}

	@Test
	public void shouldMapOkMinimal() {

		HentForsendelseResponseTo.HentForsendelseResponseToBuilder hentForsendelseResponseToBuilder =
				createHentForsendelseResponsToBuilder();
		hentForsendelseResponseToBuilder
				.bestillingsId(null)
				.dokumenter(Arrays.asList(HentForsendelseResponseTo.DokumentTo.builder()
						.tilknyttetSom(TILKNYTTET_SOM_HOVEDDOK)
						.dokumentObjektReferanse(DOKUMENT_OBJEKT_REFERANSE_1)
						.arkivDokumentInfoId(ARKIV_DOKUMENTINFO_ID_1)
						.dokumenttypeId(DOKUMENTTYPE_ID_1)
						.build()));

		HentSikkerDigitalPostadresseResponseTo.HentSikkerDigitalPostadresseResponseToBuilder
				hentSikkerDigitalPostadresseResponseToBuilder = createHentSikkerDigitalPostadresseResponseToBuilder();
		hentSikkerDigitalPostadresseResponseToBuilder.digitalKontaktinformasjon(HentSikkerDigitalPostadresseResponseTo
				.Kontaktinformasjon.builder()
				.epostadresse(HentSikkerDigitalPostadresseResponseTo.Epostadresse.builder()
						.value(EPOST_VALUE)
						.sistOppdatert(UGYLDIG_SIST_OPPDATERT)
						.sistVerifisert(GYLDIG_SIST_VERIFISERT)
						.build())
				.mobiltelefonnummer(null)
				.build())
				.sertifikat(null);

		VarselInfoTo.VarselInfoToBuilder varselInfoToBuilder = createVarselInfoToBuilder();
		varselInfoToBuilder
				.antallDagerListe(null)
				.preferertKanal(makePreferertKanalSet(PREFERERT_KANAL_EPOST));

		SendDigitalPost sendDigitalPost = bridgeMotSDPMapper.map(hentForsendelseResponseToBuilder.build(),
				hentSikkerDigitalPostadresseResponseToBuilder.build(), createDokumenttypeInfoTo(),
				varselInfoToBuilder.build(), createSafJournalpostTo());

		assertSendDigitalPost(sendDigitalPost);
		assertNull(sendDigitalPost.getSendDigitalPostRequest().getSertifikat());
		assertFalse(sendDigitalPost.getSendDigitalPostRequest().getManifest().getVedlegg().stream().findFirst().isPresent());

		JAXBElement<DigitalPost> jaxbDigitalPost = (JAXBElement<DigitalPost>) sendDigitalPost.getSendDigitalPostRequest()
				.getStandardBusinessDocument().getAny();
		Varsler varsler = jaxbDigitalPost.getValue().getDigitalPostInfo().getVarsler();

		assertNull(varsler.getSmsVarsel());
		assertNull(varsler.getEpostVarsel().getRepetisjoner());
		assertEquals(varsler.getEpostVarsel().getVarslingsTekst().getValue(), EPOST_VARSLINGS_TEKST);
		assertEquals(varsler.getEpostVarsel().getEpostadresse(), EPOST_VALUE);
	}

	@Test
	public void shouldMapOkWithPreferertKanalEpostWithSMS() {
		HentSikkerDigitalPostadresseResponseTo.HentSikkerDigitalPostadresseResponseToBuilder
				hentSikkerDigitalPostadresseResponseToBuilder = createHentSikkerDigitalPostadresseResponseToBuilder();
		hentSikkerDigitalPostadresseResponseToBuilder.digitalKontaktinformasjon(HentSikkerDigitalPostadresseResponseTo
				.Kontaktinformasjon.builder()
				.epostadresse(null)
				.mobiltelefonnummer(HentSikkerDigitalPostadresseResponseTo.Mobiltelefonnummer.builder()
						.value(MOBIL_VALUE)
						.sistOppdatert(GYLDIG_SIST_OPPDATERT)
						.sistVerifisert(GYLDIG_SIST_VERIFISERT)
						.build())
				.build());

		VarselInfoTo.VarselInfoToBuilder varselInfoToBuilder = createVarselInfoToBuilder();
		varselInfoToBuilder.preferertKanal(makePreferertKanalSet(PREFERERT_KANAL_EPOST));

		SendDigitalPost sendDigitalPost = bridgeMotSDPMapper.map(createHentForsendelseResponsToBuilder().build(),
				hentSikkerDigitalPostadresseResponseToBuilder.build(), createDokumenttypeInfoTo(),
				varselInfoToBuilder.build(), createSafJournalpostTo());

		assertSendDigitalPost(sendDigitalPost);
		assertEpostAndSMSVarsel(sendDigitalPost, false, true);
	}

	@Test
	public void shouldMapOkWithPreferertKanalEpostWithEpostAndSMS() {
		HentSikkerDigitalPostadresseResponseTo.HentSikkerDigitalPostadresseResponseToBuilder
				hentSikkerDigitalPostadresseResponseToBuilder = createHentSikkerDigitalPostadresseResponseToBuilder();
		hentSikkerDigitalPostadresseResponseToBuilder.digitalKontaktinformasjon(HentSikkerDigitalPostadresseResponseTo
				.Kontaktinformasjon.builder()
				.epostadresse(HentSikkerDigitalPostadresseResponseTo.Epostadresse.builder()
						.value(EPOST_VALUE)
						.sistOppdatert(GYLDIG_SIST_OPPDATERT)
						.sistVerifisert(GYLDIG_SIST_VERIFISERT)
						.build())
				.mobiltelefonnummer(HentSikkerDigitalPostadresseResponseTo.Mobiltelefonnummer.builder()
						.value(MOBIL_VALUE)
						.sistOppdatert(GYLDIG_SIST_OPPDATERT)
						.sistVerifisert(GYLDIG_SIST_VERIFISERT)
						.build())
				.build());

		VarselInfoTo.VarselInfoToBuilder varselInfoToBuilder = createVarselInfoToBuilder();
		varselInfoToBuilder.preferertKanal(makePreferertKanalSet(PREFERERT_KANAL_EPOST));

		SendDigitalPost sendDigitalPost = bridgeMotSDPMapper.map(createHentForsendelseResponsToBuilder().build(),
				hentSikkerDigitalPostadresseResponseToBuilder.build(), createDokumenttypeInfoTo(),
				varselInfoToBuilder.build(), createSafJournalpostTo());

		assertSendDigitalPost(sendDigitalPost);
		assertEpostAndSMSVarsel(sendDigitalPost, true, false);
	}

	@Test
	public void shouldMapOkWithPreferertKanalSMSWithEpost() {
		HentSikkerDigitalPostadresseResponseTo.HentSikkerDigitalPostadresseResponseToBuilder
				hentSikkerDigitalPostadresseResponseToBuilder = createHentSikkerDigitalPostadresseResponseToBuilder();
		hentSikkerDigitalPostadresseResponseToBuilder.digitalKontaktinformasjon(HentSikkerDigitalPostadresseResponseTo
				.Kontaktinformasjon.builder()
				.epostadresse(HentSikkerDigitalPostadresseResponseTo.Epostadresse.builder()
						.value(EPOST_VALUE)
						.sistOppdatert(GYLDIG_SIST_OPPDATERT)
						.sistVerifisert(GYLDIG_SIST_VERIFISERT)
						.build())
				.mobiltelefonnummer(null)
				.build());

		VarselInfoTo.VarselInfoToBuilder varselInfoToBuilder = createVarselInfoToBuilder();
		varselInfoToBuilder.preferertKanal(makePreferertKanalSet(PREFERERT_KANAL_SMS));

		SendDigitalPost sendDigitalPost = bridgeMotSDPMapper.map(createHentForsendelseResponsToBuilder().build(),
				hentSikkerDigitalPostadresseResponseToBuilder.build(), createDokumenttypeInfoTo(),
				varselInfoToBuilder.build(), createSafJournalpostTo());

		assertSendDigitalPost(sendDigitalPost);
		assertEpostAndSMSVarsel(sendDigitalPost, true, false);
	}

	@Test
	public void shouldMapOkWithPreferertKanalSMSWithSMS() {
		HentSikkerDigitalPostadresseResponseTo.HentSikkerDigitalPostadresseResponseToBuilder
				hentSikkerDigitalPostadresseResponseToBuilder = createHentSikkerDigitalPostadresseResponseToBuilder();
		hentSikkerDigitalPostadresseResponseToBuilder.digitalKontaktinformasjon(HentSikkerDigitalPostadresseResponseTo
				.Kontaktinformasjon.builder()
				.epostadresse(null)
				.mobiltelefonnummer(HentSikkerDigitalPostadresseResponseTo.Mobiltelefonnummer.builder()
						.value(MOBIL_VALUE)
						.sistOppdatert(GYLDIG_SIST_OPPDATERT)
						.sistVerifisert(GYLDIG_SIST_VERIFISERT)
						.build())
				.build());

		VarselInfoTo.VarselInfoToBuilder varselInfoToBuilder = createVarselInfoToBuilder();
		varselInfoToBuilder.preferertKanal(makePreferertKanalSet(PREFERERT_KANAL_SMS));

		SendDigitalPost sendDigitalPost = bridgeMotSDPMapper.map(createHentForsendelseResponsToBuilder().build(),
				hentSikkerDigitalPostadresseResponseToBuilder.build(), createDokumenttypeInfoTo(),
				varselInfoToBuilder.build(), createSafJournalpostTo());

		assertSendDigitalPost(sendDigitalPost);
		assertEpostAndSMSVarsel(sendDigitalPost, false, true);
	}

	@Test
	public void shouldMapOkWithPreferertKanalSMSWithEpostAndSMS() {
		HentSikkerDigitalPostadresseResponseTo.HentSikkerDigitalPostadresseResponseToBuilder
				hentSikkerDigitalPostadresseResponseToBuilder = createHentSikkerDigitalPostadresseResponseToBuilder();
		hentSikkerDigitalPostadresseResponseToBuilder.digitalKontaktinformasjon(HentSikkerDigitalPostadresseResponseTo
				.Kontaktinformasjon.builder()
				.epostadresse(HentSikkerDigitalPostadresseResponseTo.Epostadresse.builder()
						.value(EPOST_VALUE)
						.sistOppdatert(GYLDIG_SIST_OPPDATERT)
						.sistVerifisert(GYLDIG_SIST_VERIFISERT)
						.build())
				.mobiltelefonnummer(HentSikkerDigitalPostadresseResponseTo.Mobiltelefonnummer.builder()
						.value(MOBIL_VALUE)
						.sistOppdatert(GYLDIG_SIST_OPPDATERT)
						.sistVerifisert(GYLDIG_SIST_VERIFISERT)
						.build())
				.build());

		VarselInfoTo.VarselInfoToBuilder varselInfoToBuilder = createVarselInfoToBuilder();
		varselInfoToBuilder.preferertKanal(makePreferertKanalSet(PREFERERT_KANAL_SMS));

		SendDigitalPost sendDigitalPost = bridgeMotSDPMapper.map(createHentForsendelseResponsToBuilder().build(),
				hentSikkerDigitalPostadresseResponseToBuilder.build(), createDokumenttypeInfoTo(),
				varselInfoToBuilder.build(), createSafJournalpostTo());

		assertSendDigitalPost(sendDigitalPost);
		assertEpostAndSMSVarsel(sendDigitalPost, false, true);
	}

	@Test
	public void shouldMapOkWithPreferertKanalSMSAndEpostWithEpost() {
		HentSikkerDigitalPostadresseResponseTo.HentSikkerDigitalPostadresseResponseToBuilder
				hentSikkerDigitalPostadresseResponseToBuilder = createHentSikkerDigitalPostadresseResponseToBuilder();
		hentSikkerDigitalPostadresseResponseToBuilder.digitalKontaktinformasjon(HentSikkerDigitalPostadresseResponseTo
				.Kontaktinformasjon.builder()
				.epostadresse(HentSikkerDigitalPostadresseResponseTo.Epostadresse.builder()
						.value(EPOST_VALUE)
						.sistOppdatert(GYLDIG_SIST_OPPDATERT)
						.sistVerifisert(GYLDIG_SIST_VERIFISERT)
						.build())
				.mobiltelefonnummer(null)
				.build());

		VarselInfoTo.VarselInfoToBuilder varselInfoToBuilder = createVarselInfoToBuilder();
		varselInfoToBuilder.preferertKanal(makePreferertKanalSet(PREFERERT_KANAL_EPOST, PREFERERT_KANAL_SMS));

		SendDigitalPost sendDigitalPost = bridgeMotSDPMapper.map(createHentForsendelseResponsToBuilder().build(),
				hentSikkerDigitalPostadresseResponseToBuilder.build(), createDokumenttypeInfoTo(),
				varselInfoToBuilder.build(), createSafJournalpostTo());

		assertSendDigitalPost(sendDigitalPost);
		assertEpostAndSMSVarsel(sendDigitalPost, true, false);
	}

	@Test
	public void shouldMapOkWithPreferertKanalSMSAndEpostWithSMS() {
		HentSikkerDigitalPostadresseResponseTo.HentSikkerDigitalPostadresseResponseToBuilder
				hentSikkerDigitalPostadresseResponseToBuilder = createHentSikkerDigitalPostadresseResponseToBuilder();
		hentSikkerDigitalPostadresseResponseToBuilder.digitalKontaktinformasjon(HentSikkerDigitalPostadresseResponseTo
				.Kontaktinformasjon.builder()
				.epostadresse(null)
				.mobiltelefonnummer(HentSikkerDigitalPostadresseResponseTo.Mobiltelefonnummer.builder()
						.value(MOBIL_VALUE)
						.sistOppdatert(GYLDIG_SIST_OPPDATERT)
						.sistVerifisert(GYLDIG_SIST_VERIFISERT)
						.build())
				.build());

		VarselInfoTo.VarselInfoToBuilder varselInfoToBuilder = createVarselInfoToBuilder();
		varselInfoToBuilder.preferertKanal(makePreferertKanalSet(PREFERERT_KANAL_EPOST, PREFERERT_KANAL_SMS));

		SendDigitalPost sendDigitalPost = bridgeMotSDPMapper.map(createHentForsendelseResponsToBuilder().build(),
				hentSikkerDigitalPostadresseResponseToBuilder.build(), createDokumenttypeInfoTo(),
				varselInfoToBuilder.build(), createSafJournalpostTo());

		assertSendDigitalPost(sendDigitalPost);
		assertEpostAndSMSVarsel(sendDigitalPost, false, true);
	}

	@Test
	public void shouldMapOkWithEpostUgyldigDato() {
		HentSikkerDigitalPostadresseResponseTo.HentSikkerDigitalPostadresseResponseToBuilder
				hentSikkerDigitalPostadresseResponseToBuilder = createHentSikkerDigitalPostadresseResponseToBuilder();
		hentSikkerDigitalPostadresseResponseToBuilder.digitalKontaktinformasjon(HentSikkerDigitalPostadresseResponseTo
				.Kontaktinformasjon.builder()
				.epostadresse(HentSikkerDigitalPostadresseResponseTo.Epostadresse.builder()
						.value(EPOST_VALUE)
						.sistOppdatert(UGYLDIG_SIST_OPPDATERT)
						.sistVerifisert(UGYLDIG_SIST_VERIFISERT)
						.build())
				.mobiltelefonnummer(HentSikkerDigitalPostadresseResponseTo.Mobiltelefonnummer.builder()
						.value(MOBIL_VALUE)
						.sistOppdatert(GYLDIG_SIST_OPPDATERT)
						.sistVerifisert(GYLDIG_SIST_VERIFISERT)
						.build())
				.build());

		VarselInfoTo.VarselInfoToBuilder varselInfoToBuilder = createVarselInfoToBuilder();
		varselInfoToBuilder.preferertKanal(makePreferertKanalSet(PREFERERT_KANAL_EPOST, PREFERERT_KANAL_SMS));

		SendDigitalPost sendDigitalPost = bridgeMotSDPMapper.map(createHentForsendelseResponsToBuilder().build(),
				hentSikkerDigitalPostadresseResponseToBuilder.build(), createDokumenttypeInfoTo(),
				varselInfoToBuilder.build(), createSafJournalpostTo());

		assertSendDigitalPost(sendDigitalPost);
		assertEpostAndSMSVarsel(sendDigitalPost, false, true);
	}

	@Test
	public void shouldMapOkWithSMSUgyldigDato() {
		HentSikkerDigitalPostadresseResponseTo.HentSikkerDigitalPostadresseResponseToBuilder
				hentSikkerDigitalPostadresseResponseToBuilder = createHentSikkerDigitalPostadresseResponseToBuilder();
		hentSikkerDigitalPostadresseResponseToBuilder.digitalKontaktinformasjon(HentSikkerDigitalPostadresseResponseTo
				.Kontaktinformasjon.builder()
				.epostadresse(HentSikkerDigitalPostadresseResponseTo.Epostadresse.builder()
						.value(EPOST_VALUE)
						.sistOppdatert(GYLDIG_SIST_OPPDATERT)
						.sistVerifisert(GYLDIG_SIST_VERIFISERT)
						.build())
				.mobiltelefonnummer(HentSikkerDigitalPostadresseResponseTo.Mobiltelefonnummer.builder()
						.value(MOBIL_VALUE)
						.sistOppdatert(UGYLDIG_SIST_OPPDATERT)
						.sistVerifisert(UGYLDIG_SIST_VERIFISERT)
						.build())
				.build());

		VarselInfoTo.VarselInfoToBuilder varselInfoToBuilder = createVarselInfoToBuilder();
		varselInfoToBuilder.preferertKanal(makePreferertKanalSet(PREFERERT_KANAL_EPOST, PREFERERT_KANAL_SMS));

		SendDigitalPost sendDigitalPost = bridgeMotSDPMapper.map(createHentForsendelseResponsToBuilder().build(),
				hentSikkerDigitalPostadresseResponseToBuilder.build(), createDokumenttypeInfoTo(),
				varselInfoToBuilder.build(), createSafJournalpostTo());

		assertSendDigitalPost(sendDigitalPost);
		assertEpostAndSMSVarsel(sendDigitalPost, true, false);
	}

	@Test
	public void shouldMapOkWithSMSAndEpostUgyldigDato() {
		HentSikkerDigitalPostadresseResponseTo.HentSikkerDigitalPostadresseResponseToBuilder
				hentSikkerDigitalPostadresseResponseToBuilder = createHentSikkerDigitalPostadresseResponseToBuilder();
		hentSikkerDigitalPostadresseResponseToBuilder.digitalKontaktinformasjon(HentSikkerDigitalPostadresseResponseTo
				.Kontaktinformasjon.builder()
				.epostadresse(HentSikkerDigitalPostadresseResponseTo.Epostadresse.builder()
						.value(EPOST_VALUE)
						.sistOppdatert(UGYLDIG_SIST_OPPDATERT)
						.sistVerifisert(UGYLDIG_SIST_VERIFISERT)
						.build())
				.mobiltelefonnummer(HentSikkerDigitalPostadresseResponseTo.Mobiltelefonnummer.builder()
						.value(MOBIL_VALUE)
						.sistOppdatert(UGYLDIG_SIST_OPPDATERT)
						.sistVerifisert(UGYLDIG_SIST_VERIFISERT)
						.build())
				.build());

		VarselInfoTo.VarselInfoToBuilder varselInfoToBuilder = createVarselInfoToBuilder();
		varselInfoToBuilder.preferertKanal(makePreferertKanalSet(PREFERERT_KANAL_EPOST, PREFERERT_KANAL_SMS));

		SendDigitalPost sendDigitalPost = bridgeMotSDPMapper.map(createHentForsendelseResponsToBuilder().build(),
				hentSikkerDigitalPostadresseResponseToBuilder.build(), createDokumenttypeInfoTo(),
				varselInfoToBuilder.build(), createSafJournalpostTo());

		assertSendDigitalPost(sendDigitalPost);

		JAXBElement<DigitalPost> jaxbDigitalPost = (JAXBElement<DigitalPost>) sendDigitalPost.getSendDigitalPostRequest()
				.getStandardBusinessDocument().getAny();
		assertNull(jaxbDigitalPost.getValue().getDigitalPostInfo().getVarsler());
	}

	private void assertEpostAndSMSVarsel(SendDigitalPost sendDigitalPost, boolean preferertKanalEpost, boolean preferertKanalSMS) {

		JAXBElement<DigitalPost> jaxbDigitalPost = (JAXBElement<DigitalPost>) sendDigitalPost.getSendDigitalPostRequest()
				.getStandardBusinessDocument().getAny();
		Varsler varsler = jaxbDigitalPost.getValue().getDigitalPostInfo().getVarsler();

		if (preferertKanalEpost && !preferertKanalSMS) {
			assertNull(varsler.getSmsVarsel());
			assertTrue(varsler.getEpostVarsel().getRepetisjoner().getDagerEtter().equals(ANTALL_DAGER_LISTE));
			assertEquals(varsler.getEpostVarsel().getVarslingsTekst().getValue(), EPOST_VARSLINGS_TEKST);
			assertEquals(varsler.getEpostVarsel().getVarslingsTekst().getLang(), SPRAAK_KODE);
			assertEquals(varsler.getEpostVarsel().getEpostadresse(), EPOST_VALUE);
		} else if (!preferertKanalEpost && preferertKanalSMS) {
			assertNull(varsler.getEpostVarsel());
			assertTrue(varsler.getSmsVarsel().getRepetisjoner().getDagerEtter().equals(ANTALL_DAGER_LISTE));
			assertEquals(varsler.getSmsVarsel().getVarslingsTekst().getValue(), SMS_VARSLINGS_TEKST);
			assertEquals(varsler.getSmsVarsel().getVarslingsTekst().getLang(), SPRAAK_KODE);
			assertEquals(varsler.getSmsVarsel().getMobiltelefonnummer(), MOBIL_VALUE);
		} else if (preferertKanalEpost && preferertKanalSMS) {
			assertTrue(varsler.getSmsVarsel().getRepetisjoner().getDagerEtter().equals(ANTALL_DAGER_LISTE));
			assertEquals(varsler.getSmsVarsel().getVarslingsTekst().getValue(), SMS_VARSLINGS_TEKST);
			assertEquals(varsler.getSmsVarsel().getVarslingsTekst().getLang(), SPRAAK_KODE);
			assertEquals(varsler.getSmsVarsel().getMobiltelefonnummer(), MOBIL_VALUE);
			assertTrue(varsler.getEpostVarsel().getRepetisjoner().getDagerEtter().equals(ANTALL_DAGER_LISTE));
			assertEquals(varsler.getEpostVarsel().getVarslingsTekst().getValue(), EPOST_VARSLINGS_TEKST);
			assertEquals(varsler.getEpostVarsel().getVarslingsTekst().getLang(), SPRAAK_KODE);
			assertEquals(varsler.getEpostVarsel().getEpostadresse(), EPOST_VALUE);
		}
	}

	private void assertSendDigitalPost(SendDigitalPost sendDigitalPost) {

		assertNotNull(sendDigitalPost);
	}

	private void assertResponse(SendDigitalPost sendDigitalPost) {

		assertSendDigitalPost(sendDigitalPost);

		//Assert SendDigitalPostRequest
		final SendDigitalPostRequest sendDigitalPostRequest = sendDigitalPost.getSendDigitalPostRequest();
		assertEquals(sendDigitalPostRequest.isErPrioritert(), ER_PRIORITERT);
		assertNotNull(sendDigitalPostRequest.getManifest());
		assertNotNull(sendDigitalPostRequest.getSertifikat());
		assertNotNull(sendDigitalPostRequest.getStandardBusinessDocument());

		//Assert Manifest
		final Manifest manifest = sendDigitalPostRequest.getManifest();
		assertEquals(manifest.getAvsender().getOrganisasjon().getValue(), ORGNR_NAV);
		assertEquals(manifest.getMottaker().getPerson().getPersonidentifikator(), MOTTAKER_ID);
		assertEquals(manifest.getMottaker().getPerson().getPostkasseadresse(), BRUKER_ADRESSE);
		assertEquals(manifest.getHoveddokument().getMime(), DOKUMENT_MIME);
		assertEquals(manifest.getHoveddokument().getHref(), DOKUMENT_OBJEKT_REFERANSE_1 + ".pdf");
		assertEquals(manifest.getHoveddokument().getTittel().getValue(), FORSENDELSE_TITTEL);
		assertEquals(manifest.getHoveddokument().getTittel().getLang(), SPRAAK_KODE);

		manifest.getVedlegg().stream().forEach(
				vedlegg -> {
					assertEquals(vedlegg.getMime(), DOKUMENT_MIME);
					assertEquals(vedlegg.getTittel().getLang(), SPRAAK_KODE);
					assertTrue(vedlegg.getHref().equals(DOKUMENT_OBJEKT_REFERANSE_2 + ".pdf") ||
							vedlegg.getHref().equals(DOKUMENT_OBJEKT_REFERANSE_3 + ".pdf"));
					assertTrue(vedlegg.getTittel().getValue().equals(TITTEL_VEDLEGG_1) ||
							vedlegg.getTittel().getValue().equals(TITTEL_VEDLEGG_2));
				}
		);

		//Assert StandardBusinessDocument
		final StandardBusinessDocument standardBusinessDocument = sendDigitalPostRequest.getStandardBusinessDocument();
		final StandardBusinessDocumentHeader standardBusinessDocumentHeader = standardBusinessDocument.getStandardBusinessDocumentHeader();
		assertNotNull(standardBusinessDocument);
		assertNotNull(standardBusinessDocumentHeader);
		assertEquals(standardBusinessDocumentHeader.getHeaderVersion(), VERSION);
		assertEquals(standardBusinessDocumentHeader.getSender()
				.stream()
				.findAny()
				.get()
				.getIdentifier()
				.getValue(), ORGNR_NAV);
		assertEquals(standardBusinessDocumentHeader.getSender()
				.stream()
				.findAny()
				.get()
				.getIdentifier()
				.getAuthority(), AUTHORITY);
		assertEquals(standardBusinessDocumentHeader.getReceiver()
				.stream()
				.findAny()
				.get()
				.getIdentifier()
				.getValue(), LEVERANDOER_ADRESSE);
		assertEquals(standardBusinessDocumentHeader.getReceiver()
				.stream()
				.findAny()
				.get()
				.getIdentifier()
				.getAuthority(), AUTHORITY);

		//Assert DocumentIdentification
		final DocumentIdentification documentIdentification = standardBusinessDocumentHeader.getDocumentIdentification();
		assertNotNull(documentIdentification);
		assertEquals(documentIdentification.getStandard(), STANDARD);
		assertEquals(documentIdentification.getTypeVersion(), VERSION);
		assertEquals(documentIdentification.getInstanceIdentifier(), BESTILLINGS_ID);
		assertEquals(documentIdentification.getType(), DIGITAL_POST);
		Assertions.assertEquals(getDateOnly(documentIdentification.getCreationDateAndTime()), getDateOnly(getNow()));

		//Assert Scope
		final Scope scope = standardBusinessDocumentHeader.getBusinessScope().getScope().stream().findFirst().get();
		assertNotNull(scope);
		assertEquals(scope.getType(), BUSINESS_SCOPE_TYPE);
		assertEquals(scope.getInstanceIdentifier(), BESTILLINGS_ID);
		assertEquals(scope.getIdentifier(), STANDARD);

		//Assert DigitalPost
		final JAXBElement<DigitalPost> jaxbDigitalPost = (JAXBElement<DigitalPost>) standardBusinessDocument.getAny();
		assertNotNull(jaxbDigitalPost);
		assertEquals(jaxbDigitalPost.getValue().getAvsender().getOrganisasjon().getValue(), ORGNR_NAV);
		assertEquals(jaxbDigitalPost.getValue().getAvsender().getOrganisasjon().getAuthority(), AUTHORITY_ENUM);
		assertEquals(jaxbDigitalPost.getValue().getMottaker().getPerson().getPersonidentifikator(), MOTTAKER_ID);
		assertEquals(jaxbDigitalPost.getValue().getMottaker().getPerson().getPostkasseadresse(), BRUKER_ADRESSE);

		//Assert DigitalPostInfo
		final DigitalPostInfo digitalPostInfo = jaxbDigitalPost.getValue().getDigitalPostInfo();
		assertNotNull(digitalPostInfo);
		assertEquals(digitalPostInfo.getIkkeSensitivTittel().getValue(), FORSENDELSE_TITTEL);
		assertEquals(digitalPostInfo.getIkkeSensitivTittel().getLang(), SPRAAK_KODE);
		assertEquals(digitalPostInfo.getSikkerhetsnivaa(), Integer.toString(SIKKERHETSNIVAA));
		assertFalse(digitalPostInfo.isAapningskvittering());

		//Assert Varsler
		final Varsler varsler = digitalPostInfo.getVarsler();
		assertNotNull(varsler);
		assertEpostAndSMSVarsel(sendDigitalPost, true, true);
	}

	private SafJournalpostTo createSafJournalpostTo() {
		return SafJournalpostTo.builder()
				.dokumenter(Arrays.asList(SafJournalpostTo.DokumentInfo.builder()
								.dokumentInfoId(ARKIV_DOKUMENTINFO_ID_2)
								.tittel(TITTEL_VEDLEGG_1)
								.build(),
						SafJournalpostTo.DokumentInfo.builder()
								.dokumentInfoId(ARKIV_DOKUMENTINFO_ID_3)
								.tittel(TITTEL_VEDLEGG_2)
								.build()))
				.build();
	}

	private VarselInfoTo.VarselInfoToBuilder createVarselInfoToBuilder() {
		return VarselInfoTo.builder()
				.varselTypeId(VARSEL_TYPE_ID)
				.stoppRepeterendeVarsel(STOPP_REPETERENDE_VARSEL)
				.varslingsTekst(varslingsTekster(EPOST_VARSLINGS_TEKST, SMS_VARSLINGS_TEKST))
				.antallDagerListe(ANTALL_DAGER_LISTE)
				.preferertKanal(makePreferertKanalSet(PREFERERT_KANAL_EPOST, PREFERERT_KANAL_SMS));
	}

	private DokumenttypeInfoTo createDokumenttypeInfoTo() {
		return DokumenttypeInfoTo.builder()
				.varselTypeId(VARSEL_TYPE_ID)
				.sikkerhetsnivaa(SIKKERHETSNIVAA)
				.build();
	}

	private HentSikkerDigitalPostadresseResponseTo.HentSikkerDigitalPostadresseResponseToBuilder createHentSikkerDigitalPostadresseResponseToBuilder() {
		return HentSikkerDigitalPostadresseResponseTo.builder()
				.digitalKontaktinformasjon(HentSikkerDigitalPostadresseResponseTo.Kontaktinformasjon.builder()
						.personident(PERSONIDENT)
						.reservasjon(INGEN_RESERVASJON)
						.epostadresse(HentSikkerDigitalPostadresseResponseTo.Epostadresse.builder()
								.value(EPOST_VALUE)
								.sistOppdatert(GYLDIG_SIST_OPPDATERT)
								.sistVerifisert(GYLDIG_SIST_VERIFISERT)
								.build())
						.mobiltelefonnummer(HentSikkerDigitalPostadresseResponseTo.Mobiltelefonnummer.builder()
								.value(MOBIL_VALUE)
								.sistOppdatert(GYLDIG_SIST_OPPDATERT)
								.sistVerifisert(GYLDIG_SIST_VERIFISERT)
								.build())
						.build())
				.sikkerDigitalPostkasse(HentSikkerDigitalPostadresseResponseTo.DigitalPostkasse.builder()
						.leverandoerAdresse(LEVERANDOER_ADRESSE)
						.brukerAdresse(BRUKER_ADRESSE)
						.build())
				.sertifikat(SERTIFIKAT);
	}

	private HentForsendelseResponseTo.HentForsendelseResponseToBuilder createHentForsendelseResponsToBuilder() {
		return HentForsendelseResponseTo.builder()
				.bestillingsId(BESTILLINGS_ID)
				.forsendelseStatus(FORSENDELSE_STATUS)
				.modus(MODUS)
				.tema(TEMA)
				.forsendelseTittel(FORSENDELSE_TITTEL)
				.arkivInformasjon(HentForsendelseResponseTo.ArkivInformasjonTo.builder()
						.arkivId(ARKIV_ID)
						.build())
				.mottaker(HentForsendelseResponseTo.MottakerTo.builder()
						.mottakerId(MOTTAKER_ID)
						.mottakerNavn(MOTTAKER_NAVN)
						.mottakerType(MOTTAKER_TYPE)
						.build())
				.arkivInformasjon(HentForsendelseResponseTo.ArkivInformasjonTo.builder()
						.arkivId(ARKIV_ID)
						.build())
				.postadresse(HentForsendelseResponseTo.PostadresseTo.builder()
						.adresselinje1(ADRESSELINJE_1)
						.adresselinje2(ADRESSELINJE_2)
						.adresselinje3(ADRESSELINJE_3)
						.postnummer(POSTNUMMER)
						.poststed(POSTSTED)
						.landkode(LAND_KODE)
						.build())
				.dokumenter(Arrays.asList(HentForsendelseResponseTo.DokumentTo.builder()
								.tilknyttetSom(TILKNYTTET_SOM_HOVEDDOK)
								.dokumentObjektReferanse(DOKUMENT_OBJEKT_REFERANSE_1)
								.arkivDokumentInfoId(ARKIV_DOKUMENTINFO_ID_1)
								.dokumenttypeId(DOKUMENTTYPE_ID_1)
								.build(),
						HentForsendelseResponseTo.DokumentTo.builder()
								.tilknyttetSom(TILKNYTTET_SOM_VEDLEGG)
								.dokumentObjektReferanse(DOKUMENT_OBJEKT_REFERANSE_2)
								.arkivDokumentInfoId(ARKIV_DOKUMENTINFO_ID_2)
								.dokumenttypeId(DOKUMENTTYPE_ID_2)
								.build(),
						HentForsendelseResponseTo.DokumentTo.builder()
								.tilknyttetSom(TILKNYTTET_SOM_VEDLEGG)
								.dokumentObjektReferanse(DOKUMENT_OBJEKT_REFERANSE_3)
								.arkivDokumentInfoId(ARKIV_DOKUMENTINFO_ID_3)
								.dokumenttypeId(DOKUMENTTYPE_ID_3)
								.build()
				));
	}
}
