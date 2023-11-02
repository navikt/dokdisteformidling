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

	String journalpostId;
	Sak sak;
	String opprettetAvNavn;
	Bruker bruker;
	LocalDateTime datoOpprettet;
	String tittel;
	String journalfortAvNavn;
	String temanavn;
	String tema;
	String journalposttype;
	String journalfoerendeEnhet;

	@Builder.Default
	List<RelevantDato> relevanteDatoer = new ArrayList<>();

	@Builder.Default
	List<DokumentInfo> dokumenter = new ArrayList<>();

	@Value
	@Builder
	public static class Sak {
		String arkivsaksnummer;
		LocalDateTime datoOpprettet;
	}

	@Value
	@Builder
	public static class RelevantDato {
		LocalDateTime dato;
		Datotype datotype;
	}

	@Value
	@Builder
	public static class Bruker {
		String id;
		String type;
	}

	@Value
	@Builder
	public static class DokumentInfo {
		String dokumentInfoId;
		String dokumentstatus;
		String tittel;
		String originalJournalpostId;
		@Builder.Default
		List<Dokumentvariant> dokumentvarianter = new ArrayList<>();

		@Value
		@Builder
		public static class Dokumentvariant {
			String variantformat;
			String filtype;
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
