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

	private final String journalpostId;
	private final Sak sak;
	private final String opprettetAvNavn;
	private final Bruker bruker;
	private final LocalDateTime datoOpprettet;
	private final String tittel;
	private final String journalfortAvNavn;
	private final String temanavn;
	private final String tema;
	private final String journalposttype;
	private final String journalfoerendeEnhet;

	@Builder.Default
	private final List<RelevantDato> relevanteDatoer = new ArrayList<>();

	@Builder.Default
	private final List<DokumentInfo> dokumenter = new ArrayList<>();

	@Value
	@Builder
	public static class Sak {
		private final String arkivsaksnummer;
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
		private final String dokumentstatus;
		private final String tittel;
		private final String originalJournalpostId;
		@Builder.Default
		private final List<Dokumentvariant> dokumentvarianter = new ArrayList<>();

		@Value
		@Builder
		public static class Dokumentvariant {
			private final String variantformat;
			private final String filtype;
		}
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
