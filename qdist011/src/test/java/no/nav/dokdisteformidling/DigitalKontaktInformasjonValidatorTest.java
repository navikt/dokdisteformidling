package no.nav.dokdisteformidling;

import static no.nav.dokdisteformidling.qdist011.Qdist011FunctionalUtils.getNow;
import static no.nav.dokdisteformidling.testUtils.makePreferertKanalSet;
import static no.nav.dokdisteformidling.testUtils.makeUgyldigDate;
import static no.nav.dokdisteformidling.testUtils.varslingsTekster;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import no.nav.dokdisteformidling.consumer.dki.HentSikkerDigitalPostadresseResponseTo;
import no.nav.dokdisteformidling.consumer.dokkat.tkat021.VarselInfoTo;
import no.nav.dokdisteformidling.exception.functional.AbstractDokdisteformidlingFunctionalException;
import no.nav.dokdisteformidling.qdist011.DigitalKontaktInformasjonValidator;
import org.junit.jupiter.api.Test;

import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Arrays;
import java.util.List;

class DigitalKontaktInformasjonValidatorTest {

	private static final String VARSEL_TYPE_ID = "varselTypeId";
	private static final boolean STOPP_REPETERENDE_VARSEL = false;
	private static final String EPOST_VARSLINGS_TEKST = "epostVarslingsTekst";
	private static final String SMS_VARSLINGS_TEKST = "smsVarslingsTekst";
	private static final List<Integer> ANTALL_DAGER_LISTE = Arrays.asList(1, 2, 3);
	private static final String PREFERERT_KANAL_SMS = "SMS";
	private static final String PREFERERT_KANAL_EPOST = "EPOST";
	private static final byte[] SERTIFIKAT = "testSertifikat".getBytes();
	private static final String PERSONIDENT = "personident";
	private static final String RESERVASJON = "NEI";
	private static final String EPOST_VALUE = "epostValue";
	private static final String MOBIL_VALUE = "mobilValue";
	private static final XMLGregorianCalendar GYLDIG_SIST_VERIFISERT = getNow();
	private static final XMLGregorianCalendar GYLDIG_SIST_OPPDATERT = getNow();
	private static final XMLGregorianCalendar UGYLDIG_SIST_VERIFISERT = makeUgyldigDate();
	private static final XMLGregorianCalendar UGYLDIG_SIST_OPPDATERT = makeUgyldigDate();
	private static final String LEVERANDOER_ADRESSE = "leverandoerAdresse";
	private static final String BRUKER_ADRESSE = "brukerAdresse";

	private final DigitalKontaktInformasjonValidator digitalKontaktInformasjonValidator = new DigitalKontaktInformasjonValidator();

	@Test
	public void shouldValidateOk() {
		digitalKontaktInformasjonValidator.validateKontaktinfo(createHentSikkerDigitalPostadresseResponseToBuilder().build(),
				createVarselInfoToBuilder().build());
	}

	@Test
	public void shouldValidateOkWitoutVarselInfoTo() {
		digitalKontaktInformasjonValidator.validateKontaktinfo(createHentSikkerDigitalPostadresseResponseToBuilder().build(), null);
	}

	@Test
	public void shouldValidateWithOneValidDateEpost() {
		HentSikkerDigitalPostadresseResponseTo.HentSikkerDigitalPostadresseResponseToBuilder hentSikkerDigitalPostadresseResponseToBuilder =
				createHentSikkerDigitalPostadresseResponseToBuilder();
		hentSikkerDigitalPostadresseResponseToBuilder.digitalKontaktinformasjon(
				HentSikkerDigitalPostadresseResponseTo.Kontaktinformasjon.builder()
						.personident(PERSONIDENT)
						.reservasjon(RESERVASJON)
						.epostadresse(HentSikkerDigitalPostadresseResponseTo.Epostadresse.builder()
								.value(EPOST_VALUE)
								.sistOppdatert(GYLDIG_SIST_OPPDATERT)
								.sistVerifisert(UGYLDIG_SIST_VERIFISERT)
								.build())
						.mobiltelefonnummer(null)
						.build());

		VarselInfoTo varselInfoTo = createVarselInfoToBuilder().build();
		assertNotNull(varselInfoTo);

		digitalKontaktInformasjonValidator.validateKontaktinfo(hentSikkerDigitalPostadresseResponseToBuilder.build(), varselInfoTo);
	}

	@Test
	public void shouldValidateWithOneValidDateSMS() {
		HentSikkerDigitalPostadresseResponseTo.HentSikkerDigitalPostadresseResponseToBuilder hentSikkerDigitalPostadresseResponseToBuilder =
				createHentSikkerDigitalPostadresseResponseToBuilder();
		hentSikkerDigitalPostadresseResponseToBuilder.digitalKontaktinformasjon(
				HentSikkerDigitalPostadresseResponseTo.Kontaktinformasjon.builder()
						.personident(PERSONIDENT)
						.reservasjon(RESERVASJON)
						.epostadresse(null)
						.mobiltelefonnummer(HentSikkerDigitalPostadresseResponseTo.Mobiltelefonnummer.builder()
								.value(MOBIL_VALUE)
								.sistOppdatert(UGYLDIG_SIST_OPPDATERT)
								.sistVerifisert(GYLDIG_SIST_VERIFISERT)
								.build())
						.build());

		VarselInfoTo varselInfoTo = createVarselInfoToBuilder().build();
		assertNotNull(varselInfoTo);

		digitalKontaktInformasjonValidator.validateKontaktinfo(hentSikkerDigitalPostadresseResponseToBuilder.build(), varselInfoTo);
	}

	@Test
	public void shouldFailWithoutEpostAndUgyldigSMS() {

		HentSikkerDigitalPostadresseResponseTo.HentSikkerDigitalPostadresseResponseToBuilder hentSikkerDigitalPostadresseResponseToBuilder =
				createHentSikkerDigitalPostadresseResponseToBuilder();
		hentSikkerDigitalPostadresseResponseToBuilder.digitalKontaktinformasjon(
				HentSikkerDigitalPostadresseResponseTo.Kontaktinformasjon.builder()
						.personident(PERSONIDENT)
						.reservasjon(RESERVASJON)
						.epostadresse(HentSikkerDigitalPostadresseResponseTo.Epostadresse.builder()
								.value(null)
								.sistOppdatert(GYLDIG_SIST_OPPDATERT)
								.sistVerifisert(GYLDIG_SIST_VERIFISERT)
								.build())
						.mobiltelefonnummer(HentSikkerDigitalPostadresseResponseTo.Mobiltelefonnummer.builder()
								.value(MOBIL_VALUE)
								.sistOppdatert(UGYLDIG_SIST_OPPDATERT)
								.sistVerifisert(UGYLDIG_SIST_VERIFISERT)
								.build())
						.build());

		VarselInfoTo varselInfoTo = createVarselInfoToBuilder().build();
		assertNotNull(varselInfoTo);

		assertThrows(AbstractDokdisteformidlingFunctionalException.class,
				() -> digitalKontaktInformasjonValidator.validateKontaktinfo(hentSikkerDigitalPostadresseResponseToBuilder.build(), varselInfoTo),
				"Expected digitalKontaktInformasjonValidator() to throw AbstractDokdisteformidlingFunctionalException, but it didn't");
	}

	@Test
	public void shouldFailWithoutSMSAndUgyldigEpost() {

		HentSikkerDigitalPostadresseResponseTo.HentSikkerDigitalPostadresseResponseToBuilder hentSikkerDigitalPostadresseResponseToBuilder =
				createHentSikkerDigitalPostadresseResponseToBuilder();
		hentSikkerDigitalPostadresseResponseToBuilder.digitalKontaktinformasjon(
				HentSikkerDigitalPostadresseResponseTo.Kontaktinformasjon.builder()
						.personident(PERSONIDENT)
						.reservasjon(RESERVASJON)
						.epostadresse(HentSikkerDigitalPostadresseResponseTo.Epostadresse.builder()
								.value(EPOST_VALUE)
								.sistOppdatert(UGYLDIG_SIST_OPPDATERT)
								.sistVerifisert(UGYLDIG_SIST_VERIFISERT)
								.build())
						.mobiltelefonnummer(HentSikkerDigitalPostadresseResponseTo.Mobiltelefonnummer.builder()
								.value(null)
								.sistOppdatert(GYLDIG_SIST_OPPDATERT)
								.sistVerifisert(GYLDIG_SIST_VERIFISERT)
								.build())
						.build());

		VarselInfoTo varselInfoTo = createVarselInfoToBuilder().build();
		assertNotNull(varselInfoTo);

		assertThrows(AbstractDokdisteformidlingFunctionalException.class,
				() -> digitalKontaktInformasjonValidator.validateKontaktinfo(hentSikkerDigitalPostadresseResponseToBuilder.build(), varselInfoTo),
				"Expected digitalKontaktInformasjonValidator() to throw AbstractDokdisteformidlingFunctionalException, but it didn't");
	}

	@Test
	public void shouldFailWithoutEpostAndSMS() {

		HentSikkerDigitalPostadresseResponseTo.HentSikkerDigitalPostadresseResponseToBuilder hentSikkerDigitalPostadresseResponseToBuilder =
				createHentSikkerDigitalPostadresseResponseToBuilder();
		hentSikkerDigitalPostadresseResponseToBuilder.digitalKontaktinformasjon(
				HentSikkerDigitalPostadresseResponseTo.Kontaktinformasjon.builder()
						.personident(PERSONIDENT)
						.reservasjon(RESERVASJON)
						.epostadresse(HentSikkerDigitalPostadresseResponseTo.Epostadresse.builder()
								.value(null)
								.sistOppdatert(GYLDIG_SIST_OPPDATERT)
								.sistVerifisert(GYLDIG_SIST_VERIFISERT)
								.build())
						.mobiltelefonnummer(HentSikkerDigitalPostadresseResponseTo.Mobiltelefonnummer.builder()
								.value(null)
								.sistOppdatert(GYLDIG_SIST_OPPDATERT)
								.sistVerifisert(GYLDIG_SIST_VERIFISERT)
								.build())
						.build());

		VarselInfoTo varselInfoTo = createVarselInfoToBuilder().build();
		assertNotNull(varselInfoTo);

		assertThrows(AbstractDokdisteformidlingFunctionalException.class,
				() -> digitalKontaktInformasjonValidator.validateKontaktinfo(hentSikkerDigitalPostadresseResponseToBuilder.build(), varselInfoTo),
				"Expected digitalKontaktInformasjonValidator() to throw AbstractDokdisteformidlingFunctionalException, but it didn't");
	}

	@Test
	public void shouldFailWithUgyldigEpostAndSMS() {

		HentSikkerDigitalPostadresseResponseTo.HentSikkerDigitalPostadresseResponseToBuilder hentSikkerDigitalPostadresseResponseToBuilder =
				createHentSikkerDigitalPostadresseResponseToBuilder();
		hentSikkerDigitalPostadresseResponseToBuilder.digitalKontaktinformasjon(
				HentSikkerDigitalPostadresseResponseTo.Kontaktinformasjon.builder()
						.personident(PERSONIDENT)
						.reservasjon(RESERVASJON)
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

		VarselInfoTo varselInfoTo = createVarselInfoToBuilder().build();
		assertNotNull(varselInfoTo);

		assertThrows(AbstractDokdisteformidlingFunctionalException.class,
				() -> digitalKontaktInformasjonValidator.validateKontaktinfo(hentSikkerDigitalPostadresseResponseToBuilder.build(), varselInfoTo),
				"Expected digitalKontaktInformasjonValidator() to throw AbstractDokdisteformidlingFunctionalException, but it didn't");
	}

	@Test
	public void shouldFailWithReservasjon() {

		HentSikkerDigitalPostadresseResponseTo.HentSikkerDigitalPostadresseResponseToBuilder hentSikkerDigitalPostadresseResponseToBuilder =
				createHentSikkerDigitalPostadresseResponseToBuilder();
		hentSikkerDigitalPostadresseResponseToBuilder.digitalKontaktinformasjon(
				HentSikkerDigitalPostadresseResponseTo.Kontaktinformasjon.builder()
						.personident(PERSONIDENT)
						.reservasjon("JA")
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

		assertThrows(AbstractDokdisteformidlingFunctionalException.class,
				() -> digitalKontaktInformasjonValidator.validateKontaktinfo(hentSikkerDigitalPostadresseResponseToBuilder.build(), createVarselInfoToBuilder()
						.build()),
				"Expected digitalKontaktInformasjonValidator() to throw AbstractDokdisteformidlingFunctionalException, but it didn't");
	}

	@Test
	public void shouldFailNoSertifikat() {
		HentSikkerDigitalPostadresseResponseTo.HentSikkerDigitalPostadresseResponseToBuilder hentSikkerDigitalPostadresseResponseToBuilder =
				createHentSikkerDigitalPostadresseResponseToBuilder();
		hentSikkerDigitalPostadresseResponseToBuilder.sertifikat(null);

		assertThrows(AbstractDokdisteformidlingFunctionalException.class,
				() -> digitalKontaktInformasjonValidator.validateKontaktinfo(hentSikkerDigitalPostadresseResponseToBuilder.build(), createVarselInfoToBuilder()
						.build()),
				"Expected digitalKontaktInformasjonValidator() to throw AbstractDokdisteformidlingFunctionalException, but it didn't");
	}

	@Test
	public void shouldFailNoLeverandoerAdresse() {
		HentSikkerDigitalPostadresseResponseTo.HentSikkerDigitalPostadresseResponseToBuilder hentSikkerDigitalPostadresseResponseToBuilder =
				createHentSikkerDigitalPostadresseResponseToBuilder();
		hentSikkerDigitalPostadresseResponseToBuilder.sikkerDigitalPostkasse(HentSikkerDigitalPostadresseResponseTo.DigitalPostkasse
				.builder()
				.leverandoerAdresse(null)
				.brukerAdresse(BRUKER_ADRESSE)
				.build());

		assertThrows(AbstractDokdisteformidlingFunctionalException.class,
				() -> digitalKontaktInformasjonValidator.validateKontaktinfo(hentSikkerDigitalPostadresseResponseToBuilder.build(), createVarselInfoToBuilder()
						.build()),
				"Expected digitalKontaktInformasjonValidator() to throw AbstractDokdisteformidlingFunctionalException, but it didn't");
	}

	@Test
	public void shouldFailNoBrukerAdresse() {
		HentSikkerDigitalPostadresseResponseTo.HentSikkerDigitalPostadresseResponseToBuilder hentSikkerDigitalPostadresseResponseToBuilder =
				createHentSikkerDigitalPostadresseResponseToBuilder();
		hentSikkerDigitalPostadresseResponseToBuilder.sikkerDigitalPostkasse(HentSikkerDigitalPostadresseResponseTo.DigitalPostkasse
				.builder()
				.leverandoerAdresse(LEVERANDOER_ADRESSE)
				.brukerAdresse(null)
				.build());

		assertThrows(AbstractDokdisteformidlingFunctionalException.class,
				() -> digitalKontaktInformasjonValidator.validateKontaktinfo(hentSikkerDigitalPostadresseResponseToBuilder.build(), createVarselInfoToBuilder()
						.build()),
				"Expected digitalKontaktInformasjonValidator() to throw AbstractDokdisteformidlingFunctionalException, but it didn't");
	}

	private HentSikkerDigitalPostadresseResponseTo.HentSikkerDigitalPostadresseResponseToBuilder createHentSikkerDigitalPostadresseResponseToBuilder() {
		return HentSikkerDigitalPostadresseResponseTo.builder()
				.digitalKontaktinformasjon(HentSikkerDigitalPostadresseResponseTo.Kontaktinformasjon.builder()
						.personident(PERSONIDENT)
						.reservasjon(RESERVASJON)
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

	private VarselInfoTo.VarselInfoToBuilder createVarselInfoToBuilder() {
		return VarselInfoTo.builder()
				.varselTypeId(VARSEL_TYPE_ID)
				.stoppRepeterendeVarsel(STOPP_REPETERENDE_VARSEL)
				.varslingsTekst(varslingsTekster(EPOST_VARSLINGS_TEKST, SMS_VARSLINGS_TEKST))
				.antallDagerListe(ANTALL_DAGER_LISTE)
				.preferertKanal(makePreferertKanalSet(PREFERERT_KANAL_EPOST, PREFERERT_KANAL_SMS));
	}

}