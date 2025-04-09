package no.nav.dokdisteformidling.qdist013.saf.main;

import no.nav.dokdisteformidling.consumer.saf.journalpost.SafJournalpost;

import java.time.LocalDateTime;
import java.util.Arrays;

import static java.util.Collections.singletonList;

public abstract class SafJournalpostTest {

	private final static String AVSENDER_NAVN = "avsenderNavn";
	protected final static String BRUKER_ID = "brukerId";
	protected final static String BRUKER_TYPE = "brukerType";
	protected final static LocalDateTime DATO_OPPRETTET_JP = LocalDateTime.now().minusHours(3);
	protected final static String JOURNALFOERENDEENHET = "journalfoerendeEnhet";
	protected final static String JOURNALFORT_AV_NAVN = "journalfortAvNavn";
	protected final static String JOURNALPOST_ID = "journalpostId";
	protected final static String JOURNALPOST_TYPE = "journalposttype";
	protected final static String OPPRETTET_AV_NAVN = "opprettetAvNavn";
	protected final static String TEMANAVN = "temanavn";
	protected final static String TEMA = "DAG";
	protected final static String TITTEL = "tittel";
	protected final static LocalDateTime DATO_OPPRETTET_SAK = LocalDateTime.now().minusHours(2);
	protected final static String ORIGINAL_JOURNALPOST_ID = "originalJournalpostId";

	protected final static String HOVEDDOK_DOKUMENT_INFO_ID = "hovedDokumentInfoId";
	protected final static String HOVEDDOK_TITTEL = "hovedDokumentTittel";

	protected final static String VEDLEGG_DOKUMENT_INFO_ID = "vedleggDokumentInfoId";
	protected final static String VEDLEGG_TITTEL = "vedleggTittel";

	protected final static String FILTYPE_XML = "XML";
	protected final static String VARIANTFORMAT_ORIGINAL = "ORIGINAL";
	protected final static String FILTYPE_PDF = "PDF";
	protected final static String VARIANTFORMAT_ARKIV = "ARKIV";

	private final static LocalDateTime REGISTRERT_DATO = LocalDateTime.now().minusDays(2);
	private final static String REGISTRERT_DATOTYPE = "DATO_REGISTRERT";
	protected final static LocalDateTime JOURNALFOERT_DATO = LocalDateTime.now().minusDays(1);
	private final static String JOURNALFOERT_DATOTYPE = "DATO_JOURNALFOERT";

	protected SafJournalpost.SafJournalpostBuilder createSafJournalpost() {
		return SafJournalpost.builder()
				.avsenderMottaker(SafJournalpost.AvsenderMottaker.builder().navn(AVSENDER_NAVN).build())
				.bruker(SafJournalpost.Bruker.builder().id(BRUKER_ID).type(BRUKER_TYPE).build())
				.datoOpprettet(DATO_OPPRETTET_JP)
				.dokumenter(Arrays.asList(createHovedDokumentInfo().build(), createVedleggDokumentInfo()))
				.journalfoerendeEnhet(JOURNALFOERENDEENHET)
				.journalfortAvNavn(JOURNALFORT_AV_NAVN)
				.journalpostId(JOURNALPOST_ID)
				.journalposttype(JOURNALPOST_TYPE)
				.opprettetAvNavn(OPPRETTET_AV_NAVN)
				.relevanteDatoer(Arrays.asList(createRelevantDatoRegistrert(), createRelevantDatoJournalfoert()))
				.sak(SafJournalpost.Sak.builder().arkivsaksnummer("1234568").datoOpprettet(DATO_OPPRETTET_SAK).build())
				.temanavn(TEMANAVN)
				.tittel(TITTEL);
	}

	protected SafJournalpost.DokumentInfo.DokumentInfoBuilder createHovedDokumentInfo() {
		return SafJournalpost.DokumentInfo.builder()
				.dokumentInfoId(HOVEDDOK_DOKUMENT_INFO_ID)
				.dokumentvarianter(Arrays.asList(createDokumentVariantOriginal(), createDokumentVariantArkiv()))
				.tittel(HOVEDDOK_TITTEL);
	}

	private SafJournalpost.DokumentInfo createVedleggDokumentInfo() {
		return SafJournalpost.DokumentInfo.builder()
				.dokumentInfoId(VEDLEGG_DOKUMENT_INFO_ID)
				.dokumentvarianter(singletonList(createDokumentVariantArkiv()))
				.originalJournalpostId(ORIGINAL_JOURNALPOST_ID)
				.tittel(VEDLEGG_TITTEL)
				.build();
	}

	protected SafJournalpost.DokumentInfo.Dokumentvariant createDokumentVariantOriginal() {
		return SafJournalpost.DokumentInfo.Dokumentvariant.builder()
				.filtype(FILTYPE_XML)
				.variantformat(VARIANTFORMAT_ORIGINAL)
				.build();
	}

	private SafJournalpost.DokumentInfo.Dokumentvariant createDokumentVariantArkiv() {
		return SafJournalpost.DokumentInfo.Dokumentvariant.builder()
				.filtype(FILTYPE_PDF)
				.variantformat(VARIANTFORMAT_ARKIV)
				.build();
	}

	protected SafJournalpost.RelevantDato createRelevantDatoRegistrert() {
		return SafJournalpost.RelevantDato.builder()
				.dato(REGISTRERT_DATO)
				.datotype(REGISTRERT_DATOTYPE)
				.build();
	}

	private SafJournalpost.RelevantDato createRelevantDatoJournalfoert() {
		return SafJournalpost.RelevantDato.builder()
				.dato(JOURNALFOERT_DATO)
				.datotype(JOURNALFOERT_DATOTYPE)
				.build();
	}
}
