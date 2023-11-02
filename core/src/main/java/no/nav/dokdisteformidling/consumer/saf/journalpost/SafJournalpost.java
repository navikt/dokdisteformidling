package no.nav.dokdisteformidling.consumer.saf.journalpost;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Value
@Builder
public class SafJournalpost {

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
	AvsenderMottaker avsenderMottaker;

	@Builder.Default
	List<RelevantDato> relevanteDatoer = new ArrayList<>();

	@Builder.Default
	List<DokumentInfo> dokumenter = new ArrayList<>();

	@Value
	@Builder
	public static class AvsenderMottaker {
		String navn;
	}

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
		String datotype;
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

}
