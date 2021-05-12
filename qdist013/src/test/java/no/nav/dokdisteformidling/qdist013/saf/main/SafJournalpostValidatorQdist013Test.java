package no.nav.dokdisteformidling.qdist013.saf.main;

import static org.junit.jupiter.api.Assertions.assertThrows;

import no.nav.dokdisteformidling.consumer.saf.journalpost.SafJournalpost;
import no.nav.dokdisteformidling.exception.functional.SafJournalpostValidationException;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

/**
 * @author Erik Bråten, Visma Consulting.
 */
class SafJournalpostValidatorQdist013Test extends SafJournalpostTest {

	private static final String JOURNALPOST_ID = "123456";
	private final SafJournalpostValidatorQdist013 validator = new SafJournalpostValidatorQdist013();

	@Test
	void shouldValidate() {
		validator.validate(createSafJournalpost().build(), JOURNALPOST_ID);
	}

	@Test
	void shouldValidateWhenMissingAvsenderMottaker() {
		SafJournalpost.SafJournalpostBuilder safJournalpostBuilder = createSafJournalpost();
		SafJournalpost safJournalpost = safJournalpostBuilder.avsenderMottaker(null).build();
		validator.validate(safJournalpost, JOURNALPOST_ID);
	}

	@Test
	void shouldThrowWhenMissingDokumenter() {
		SafJournalpost.SafJournalpostBuilder safJournalpostBuilder = createSafJournalpost();
		SafJournalpost safJournalpost = safJournalpostBuilder.dokumenter(null).build();
		assertThrows(SafJournalpostValidationException.class, () ->
				validator.validate(safJournalpost, JOURNALPOST_ID));
	}

	@Test
	void shouldThrowWhenMissingJournalpostId() {
		SafJournalpost.SafJournalpostBuilder safJournalpostBuilder = createSafJournalpost();
		SafJournalpost safJournalpost = safJournalpostBuilder.journalpostId(null).build();
		assertThrows(SafJournalpostValidationException.class, () ->
				validator.validate(safJournalpost, JOURNALPOST_ID));
	}

	@Test
	void shouldThrowWhenEmptyOpprettetAvNavn() {
		SafJournalpost.SafJournalpostBuilder safJournalpostBuilder = createSafJournalpost();
		SafJournalpost safJournalpost = safJournalpostBuilder.opprettetAvNavn("").build();
		assertThrows(SafJournalpostValidationException.class, () ->
				validator.validate(safJournalpost, JOURNALPOST_ID));
	}

	@Test
	void shouldThrowWhenMissingDatoOpprettet() {
		SafJournalpost.SafJournalpostBuilder safJournalpostBuilder = createSafJournalpost();
		SafJournalpost safJournalpost = safJournalpostBuilder.datoOpprettet(null).build();
		assertThrows(SafJournalpostValidationException.class, () ->
				validator.validate(safJournalpost, JOURNALPOST_ID));
	}

	@Test
	void shouldThrowWhenEmptyTittel() {
		SafJournalpost.SafJournalpostBuilder safJournalpostBuilder = createSafJournalpost();
		SafJournalpost safJournalpost = safJournalpostBuilder.tittel("").build();
		assertThrows(SafJournalpostValidationException.class, () ->
				validator.validate(safJournalpost, JOURNALPOST_ID));
	}

	@Test
	void shouldThrowWhenMissingJournalfortAvNavn() {
		SafJournalpost.SafJournalpostBuilder safJournalpostBuilder = createSafJournalpost();
		SafJournalpost safJournalpost = safJournalpostBuilder.journalfortAvNavn(null).build();
		assertThrows(SafJournalpostValidationException.class, () ->
				validator.validate(safJournalpost, JOURNALPOST_ID));
	}

	@Test
	void shouldThrowWhenEmptyTemanavn() {
		SafJournalpost.SafJournalpostBuilder safJournalpostBuilder = createSafJournalpost();
		SafJournalpost safJournalpost = safJournalpostBuilder.temanavn("").build();
		assertThrows(SafJournalpostValidationException.class, () ->
				validator.validate(safJournalpost, JOURNALPOST_ID));
	}

	@Test
	void shouldThrowWhenMissingJournalposttype() {
		SafJournalpost.SafJournalpostBuilder safJournalpostBuilder = createSafJournalpost();
		SafJournalpost safJournalpost = safJournalpostBuilder.journalposttype(null).build();
		assertThrows(SafJournalpostValidationException.class, () ->
				validator.validate(safJournalpost, JOURNALPOST_ID));
	}

	@Test
	void shouldThrowWhenEmptyJournalfoerendeEnhet() {
		SafJournalpost.SafJournalpostBuilder safJournalpostBuilder = createSafJournalpost();
		SafJournalpost safJournalpost = safJournalpostBuilder.journalfoerendeEnhet("").build();
		assertThrows(SafJournalpostValidationException.class, () ->
				validator.validate(safJournalpost, JOURNALPOST_ID));
	}

	@Test
	void shouldThrowWhenMissingBruker() {
		SafJournalpost.SafJournalpostBuilder safJournalpostBuilder = createSafJournalpost();
		SafJournalpost safJournalpost = safJournalpostBuilder.bruker(null).build();
		assertThrows(SafJournalpostValidationException.class, () ->
				validator.validate(safJournalpost, JOURNALPOST_ID));
	}

	@Test
	void shouldThrowWhenMissingBrukerId() {
		SafJournalpost.SafJournalpostBuilder safJournalpostBuilder = createSafJournalpost();
		SafJournalpost safJournalpost = safJournalpostBuilder
				.bruker(SafJournalpost.Bruker.builder().id(null).type(BRUKER_TYPE).build())
				.build();
		assertThrows(SafJournalpostValidationException.class, () ->
				validator.validate(safJournalpost, JOURNALPOST_ID));
	}

	@Test
	void shouldThrowWhenEmptyBrukerType() {
		SafJournalpost.SafJournalpostBuilder safJournalpostBuilder = createSafJournalpost();
		SafJournalpost safJournalpost = safJournalpostBuilder
				.bruker(SafJournalpost.Bruker.builder().id(BRUKER_ID).type("").build())
				.build();
		assertThrows(SafJournalpostValidationException.class, () ->
				validator.validate(safJournalpost, JOURNALPOST_ID));
	}

	@Test
	void shouldThrowWhenMissingSak() {
		SafJournalpost.SafJournalpostBuilder safJournalpostBuilder = createSafJournalpost();
		SafJournalpost safJournalpost = safJournalpostBuilder.sak(null).build();
		assertThrows(SafJournalpostValidationException.class, () ->
				validator.validate(safJournalpost, JOURNALPOST_ID));
	}

	@Test
	void shouldThrowWhenMissingSakDatoOpprettet() {
		SafJournalpost.SafJournalpostBuilder safJournalpostBuilder = createSafJournalpost();
		SafJournalpost safJournalpost = safJournalpostBuilder
				.sak(SafJournalpost.Sak.builder().datoOpprettet(null).build())
				.build();
		assertThrows(SafJournalpostValidationException.class, () ->
				validator.validate(safJournalpost, JOURNALPOST_ID));
	}

	@Test
	void shouldThrowWhenMissingRelevanteDatoer() {
		SafJournalpost.SafJournalpostBuilder safJournalpostBuilder = createSafJournalpost();
		SafJournalpost safJournalpost = safJournalpostBuilder.relevanteDatoer(null).build();
		assertThrows(SafJournalpostValidationException.class, () ->
				validator.validate(safJournalpost, JOURNALPOST_ID));
	}

	@Test
	void shouldThrowWhenRelevanteDatoerMissingDatoJournalfoert() {
		SafJournalpost.SafJournalpostBuilder safJournalpostBuilder = createSafJournalpost();
		SafJournalpost safJournalpost = safJournalpostBuilder
				.relevanteDatoer(Collections.singletonList(createRelevantDatoRegistrert()))
				.build();
		assertThrows(SafJournalpostValidationException.class, () ->
				validator.validate(safJournalpost, JOURNALPOST_ID));
	}

	@Test
	void shouldThrowWhenMissingDokumentInfoDokumentInfoid() {
		SafJournalpost.SafJournalpostBuilder safJournalpostBuilder = createSafJournalpost();
		SafJournalpost.DokumentInfo.DokumentInfoBuilder dokumentInfoBuilder = createHovedDokumentInfo();
		SafJournalpost.DokumentInfo dokumentInfo = dokumentInfoBuilder.dokumentInfoId(null).build();
		SafJournalpost safJournalpost = safJournalpostBuilder
				.dokumenter(Collections.singletonList(dokumentInfo))
				.build();
		assertThrows(SafJournalpostValidationException.class, () ->
				validator.validate(safJournalpost, JOURNALPOST_ID));
	}

	@Test
	void shouldThrowWhenEmptyDokumentInfoTittel() {
		SafJournalpost.SafJournalpostBuilder safJournalpostBuilder = createSafJournalpost();
		SafJournalpost.DokumentInfo.DokumentInfoBuilder dokumentInfoBuilder = createHovedDokumentInfo();
		SafJournalpost.DokumentInfo dokumentInfo = dokumentInfoBuilder.tittel("").build();
		SafJournalpost safJournalpost = safJournalpostBuilder
				.dokumenter(Collections.singletonList(dokumentInfo))
				.build();
		assertThrows(SafJournalpostValidationException.class, () ->
				validator.validate(safJournalpost, JOURNALPOST_ID));
	}

	@Test
	void shouldThrowWhenMissingDokumentInfoDokumentvarianter() {
		SafJournalpost.SafJournalpostBuilder safJournalpostBuilder = createSafJournalpost();
		SafJournalpost.DokumentInfo.DokumentInfoBuilder dokumentInfoBuilder = createHovedDokumentInfo();
		SafJournalpost.DokumentInfo dokumentInfo = dokumentInfoBuilder.dokumentvarianter(null).build();
		SafJournalpost safJournalpost = safJournalpostBuilder
				.dokumenter(Collections.singletonList(dokumentInfo))
				.build();
		assertThrows(SafJournalpostValidationException.class, () ->
				validator.validate(safJournalpost, JOURNALPOST_ID));
	}

	@Test
	void shouldThrowWhenMissingDokumentInfoDokumentvariantSladdetOrArkiv() {
		SafJournalpost.SafJournalpostBuilder safJournalpostBuilder = createSafJournalpost();
		SafJournalpost.DokumentInfo.DokumentInfoBuilder dokumentInfoBuilder = createHovedDokumentInfo();
		SafJournalpost.DokumentInfo dokumentInfo = dokumentInfoBuilder
				.dokumentvarianter(Collections.singletonList(createDokumentVariantOriginal()))
				.build();
		SafJournalpost safJournalpost = safJournalpostBuilder
				.dokumenter(Collections.singletonList(dokumentInfo))
				.build();
		assertThrows(SafJournalpostValidationException.class, () ->
				validator.validate(safJournalpost, JOURNALPOST_ID));
	}
}
