package no.nav.dokdisteformidling.qdist013.saf.main;

import static org.junit.jupiter.api.Assertions.assertThrows;

import no.nav.dokdisteformidling.consumer.saf.journalpost.SafJournalpost;
import no.nav.dokdisteformidling.exception.functional.SafJournalpostValidationException;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

/**
 * @author Erik Bråten, Visma Consulting.
 */
public class SafJournalpostValidatorQdist013Test extends SafJournalpostTest {

	private static String JOURNALPOST_ID = "123456";
	private final SafJournalpostValidatorQdist013 validator = new SafJournalpostValidatorQdist013();

	@Test
	public void shouldValidate() {
		validator.validate(createSafJournalpost().build(), JOURNALPOST_ID);
	}

	@Test
	public void shouldValidateWhenMissingAvsenderMottaker() {
		SafJournalpost.SafJournalpostBuilder safJournalpostBuilder = createSafJournalpost();
		SafJournalpost safJournalpost = safJournalpostBuilder.avsenderMottaker(null).build();
		validator.validate(safJournalpost, JOURNALPOST_ID);
	}

	@Test
	public void shouldThrowWhenMissingDokumenter() {
		SafJournalpost.SafJournalpostBuilder safJournalpostBuilder = createSafJournalpost();
		SafJournalpost safJournalpost = safJournalpostBuilder.dokumenter(null).build();
		assertThrows(SafJournalpostValidationException.class, () ->
				validator.validate(safJournalpost, JOURNALPOST_ID));
	}

	@Test
	public void shouldThrowWhenMissingJournalpostId() {
		SafJournalpost.SafJournalpostBuilder safJournalpostBuilder = createSafJournalpost();
		SafJournalpost safJournalpost = safJournalpostBuilder.journalpostId(null).build();
		assertThrows(SafJournalpostValidationException.class, () ->
				validator.validate(safJournalpost, JOURNALPOST_ID));
	}

	@Test
	public void shouldThrowWhenEmptyOpprettetAvNavn() {
		SafJournalpost.SafJournalpostBuilder safJournalpostBuilder = createSafJournalpost();
		SafJournalpost safJournalpost = safJournalpostBuilder.opprettetAvNavn("").build();
		assertThrows(SafJournalpostValidationException.class, () ->
				validator.validate(safJournalpost, JOURNALPOST_ID));
	}

	@Test
	public void shouldThrowWhenMissingDatoOpprettet() {
		SafJournalpost.SafJournalpostBuilder safJournalpostBuilder = createSafJournalpost();
		SafJournalpost safJournalpost = safJournalpostBuilder.datoOpprettet(null).build();
		assertThrows(SafJournalpostValidationException.class, () ->
				validator.validate(safJournalpost, JOURNALPOST_ID));
	}

	@Test
	public void shouldThrowWhenEmptyTittel() {
		SafJournalpost.SafJournalpostBuilder safJournalpostBuilder = createSafJournalpost();
		SafJournalpost safJournalpost = safJournalpostBuilder.tittel("").build();
		assertThrows(SafJournalpostValidationException.class, () ->
				validator.validate(safJournalpost, JOURNALPOST_ID));
	}

	@Test
	public void shouldThrowWhenMissingJournalfortAvNavn() {
		SafJournalpost.SafJournalpostBuilder safJournalpostBuilder = createSafJournalpost();
		SafJournalpost safJournalpost = safJournalpostBuilder.journalfortAvNavn(null).build();
		assertThrows(SafJournalpostValidationException.class, () ->
				validator.validate(safJournalpost, JOURNALPOST_ID));
	}

	@Test
	public void shouldThrowWhenEmptyTemanavn() {
		SafJournalpost.SafJournalpostBuilder safJournalpostBuilder = createSafJournalpost();
		SafJournalpost safJournalpost = safJournalpostBuilder.temanavn("").build();
		assertThrows(SafJournalpostValidationException.class, () ->
				validator.validate(safJournalpost, JOURNALPOST_ID));
	}

	@Test
	public void shouldThrowWhenMissingJournalposttype() {
		SafJournalpost.SafJournalpostBuilder safJournalpostBuilder = createSafJournalpost();
		SafJournalpost safJournalpost = safJournalpostBuilder.journalposttype(null).build();
		assertThrows(SafJournalpostValidationException.class, () ->
				validator.validate(safJournalpost, JOURNALPOST_ID));
	}

	@Test
	public void shouldThrowWhenEmptyJournalfoerendeEnhet() {
		SafJournalpost.SafJournalpostBuilder safJournalpostBuilder = createSafJournalpost();
		SafJournalpost safJournalpost = safJournalpostBuilder.journalfoerendeEnhet("").build();
		assertThrows(SafJournalpostValidationException.class, () ->
				validator.validate(safJournalpost, JOURNALPOST_ID));
	}

	@Test
	public void shouldThrowWhenMissingBruker() {
		SafJournalpost.SafJournalpostBuilder safJournalpostBuilder = createSafJournalpost();
		SafJournalpost safJournalpost = safJournalpostBuilder.bruker(null).build();
		assertThrows(SafJournalpostValidationException.class, () ->
				validator.validate(safJournalpost, JOURNALPOST_ID));
	}

	@Test
	public void shouldThrowWhenMissingBrukerId() {
		SafJournalpost.SafJournalpostBuilder safJournalpostBuilder = createSafJournalpost();
		SafJournalpost safJournalpost = safJournalpostBuilder
				.bruker(SafJournalpost.Bruker.builder().id(null).type(BRUKER_TYPE).build())
				.build();
		assertThrows(SafJournalpostValidationException.class, () ->
				validator.validate(safJournalpost, JOURNALPOST_ID));
	}

	@Test
	public void shouldThrowWhenEmptyBrukerType() {
		SafJournalpost.SafJournalpostBuilder safJournalpostBuilder = createSafJournalpost();
		SafJournalpost safJournalpost = safJournalpostBuilder
				.bruker(SafJournalpost.Bruker.builder().id(BRUKER_ID).type("").build())
				.build();
		assertThrows(SafJournalpostValidationException.class, () ->
				validator.validate(safJournalpost, JOURNALPOST_ID));
	}

	@Test
	public void shouldThrowWhenMissingSak() {
		SafJournalpost.SafJournalpostBuilder safJournalpostBuilder = createSafJournalpost();
		SafJournalpost safJournalpost = safJournalpostBuilder.sak(null).build();
		assertThrows(SafJournalpostValidationException.class, () ->
				validator.validate(safJournalpost, JOURNALPOST_ID));
	}

	@Test
	public void shouldThrowWhenMissingSakDatoOpprettet() {
		SafJournalpost.SafJournalpostBuilder safJournalpostBuilder = createSafJournalpost();
		SafJournalpost safJournalpost = safJournalpostBuilder
				.sak(SafJournalpost.Sak.builder().datoOpprettet(null).build())
				.build();
		assertThrows(SafJournalpostValidationException.class, () ->
				validator.validate(safJournalpost, JOURNALPOST_ID));
	}

	@Test
	public void shouldThrowWhenMissingRelevanteDatoer() {
		SafJournalpost.SafJournalpostBuilder safJournalpostBuilder = createSafJournalpost();
		SafJournalpost safJournalpost = safJournalpostBuilder.relevanteDatoer(null).build();
		assertThrows(SafJournalpostValidationException.class, () ->
				validator.validate(safJournalpost, JOURNALPOST_ID));
	}

	@Test
	public void shouldThrowWhenRelevanteDatoerMissingDatoJournalfoert() {
		SafJournalpost.SafJournalpostBuilder safJournalpostBuilder = createSafJournalpost();
		SafJournalpost safJournalpost = safJournalpostBuilder
				.relevanteDatoer(Arrays.asList(createRelevantDatoRegistrert()))
				.build();
		assertThrows(SafJournalpostValidationException.class, () ->
				validator.validate(safJournalpost, JOURNALPOST_ID));
	}

	@Test
	public void shouldThrowWhenMissingDokumentInfoDokumentInfoid() {
		SafJournalpost.SafJournalpostBuilder safJournalpostBuilder = createSafJournalpost();
		SafJournalpost.DokumentInfo.DokumentInfoBuilder dokumentInfoBuilder = createHovedDokumentInfo();
		SafJournalpost.DokumentInfo dokumentInfo = dokumentInfoBuilder.dokumentInfoId(null).build();
		SafJournalpost safJournalpost = safJournalpostBuilder
				.dokumenter(Arrays.asList(dokumentInfo))
				.build();
		assertThrows(SafJournalpostValidationException.class, () ->
				validator.validate(safJournalpost, JOURNALPOST_ID));
	}

	@Test
	public void shouldThrowWhenEmptyDokumentInfoTittel() {
		SafJournalpost.SafJournalpostBuilder safJournalpostBuilder = createSafJournalpost();
		SafJournalpost.DokumentInfo.DokumentInfoBuilder dokumentInfoBuilder = createHovedDokumentInfo();
		SafJournalpost.DokumentInfo dokumentInfo = dokumentInfoBuilder.tittel("").build();
		SafJournalpost safJournalpost = safJournalpostBuilder
				.dokumenter(Arrays.asList(dokumentInfo))
				.build();
		assertThrows(SafJournalpostValidationException.class, () ->
				validator.validate(safJournalpost, JOURNALPOST_ID));
	}

	@Test
	public void shouldThrowWhenMissingDokumentInfoDatoFerdigstilt() {
		SafJournalpost.SafJournalpostBuilder safJournalpostBuilder = createSafJournalpost();
		SafJournalpost.DokumentInfo.DokumentInfoBuilder dokumentInfoBuilder = createHovedDokumentInfo();
		SafJournalpost.DokumentInfo dokumentInfo = dokumentInfoBuilder.datoFerdigstilt(null).build();
		SafJournalpost safJournalpost = safJournalpostBuilder
				.dokumenter(Arrays.asList(dokumentInfo))
				.build();
		assertThrows(SafJournalpostValidationException.class, () ->
				validator.validate(safJournalpost, JOURNALPOST_ID));
	}

	@Test
	public void shouldThrowWhenMissingDokumentInfoDokumentvarianter() {
		SafJournalpost.SafJournalpostBuilder safJournalpostBuilder = createSafJournalpost();
		SafJournalpost.DokumentInfo.DokumentInfoBuilder dokumentInfoBuilder = createHovedDokumentInfo();
		SafJournalpost.DokumentInfo dokumentInfo = dokumentInfoBuilder.dokumentvarianter(null).build();
		SafJournalpost safJournalpost = safJournalpostBuilder
				.dokumenter(Arrays.asList(dokumentInfo))
				.build();
		assertThrows(SafJournalpostValidationException.class, () ->
				validator.validate(safJournalpost, JOURNALPOST_ID));
	}

	@Test
	public void shouldThrowWhenMissingDokumentInfoDokumentvariantSladdetOrArkiv() {
		SafJournalpost.SafJournalpostBuilder safJournalpostBuilder = createSafJournalpost();
		SafJournalpost.DokumentInfo.DokumentInfoBuilder dokumentInfoBuilder = createHovedDokumentInfo();
		SafJournalpost.DokumentInfo dokumentInfo = dokumentInfoBuilder
				.dokumentvarianter(Arrays.asList(createDokumentVariantOriginal()))
				.build();
		SafJournalpost safJournalpost = safJournalpostBuilder
				.dokumenter(Arrays.asList(dokumentInfo))
				.build();
		assertThrows(SafJournalpostValidationException.class, () ->
				validator.validate(safJournalpost, JOURNALPOST_ID));
	}
}
