package no.nav.dokdisteformidling.qdist013.saf.main;

import lombok.Builder;
import lombok.Value;
import no.nav.dokdisteformidling.consumer.saf.journalpost.Journalpost;
import no.nav.dokdisteformidling.exception.functional.SafJournalpostValidationException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Value
@Builder
public class JournalpostQdist013 implements Journalpost {

	private final Sak sak;
	private final String opprettetAvNavn;
	private final Bruker bruker;
	private final LocalDateTime datoOpprettet;
	private final String tittel;
	private final String kategori; //TODO: Add support in saf
	private final String journalfortAvNavn;
	private final String temanavn;

	@Builder.Default
	private final List<RelevantDato> relevanteDatoer = new ArrayList<>();

	@Builder.Default
	private final List<DokumentInfo> dokumenter = new ArrayList<>();

	@Value
	@Builder
	public static class Sak {
		private final LocalDateTime datoOpprettet;
	}

	@Value
	@Builder
	public static class RelevantDato {
		private final LocalDateTime dato;
		private final Datotype datotype;
	}

	@Value
	@Builder
	public static class Bruker {
		private final String id;
		private final String type;
	}

	@Value
	@Builder
	public static class DokumentInfo {
		private final String dokumentInfoId;
		private final String tittel;
		private final LocalDateTime datoFerdigstilt;
		private final String originalJournalpostId;
	}

	public enum Datotype {
		DATO_JOURNALFOERT
	}

	public LocalDateTime getDatoJournalfoert() {
		return this.relevanteDatoer.stream()
				.filter(relevantDato -> Datotype.DATO_JOURNALFOERT.equals(relevantDato.getDatotype()))
				.map(RelevantDato::getDato)
				.findAny()
				.orElseThrow(() -> new SafJournalpostValidationException("Kan ikke finne dato journalført for journalpost"));
	}
}
