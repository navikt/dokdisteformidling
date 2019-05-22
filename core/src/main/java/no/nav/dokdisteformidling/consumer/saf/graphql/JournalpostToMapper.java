package no.nav.dokdisteformidling.consumer.saf.graphql;

import no.nav.dokdisteformidling.consumer.saf.journalpost.Journalpost;
import no.nav.dokdisteformidling.consumer.saf.journalpost.SafJournalpostTo;

import java.util.List;
import java.util.stream.Collectors;

public class JournalpostToMapper {

	public Journalpost map(SafJournalpostTo safJournalpostTo) {
		return Journalpost.builder()
				.dokumenter(mapDokumenter(safJournalpostTo.getDokumenter()))
				.build();
	}

	private List<Journalpost.DokumentInfo> mapDokumenter(List<SafJournalpostTo.DokumentInfo> dokumenter) {
		return dokumenter
				.stream()
				.map(this::mapDokument)
				.collect(Collectors.toList());
	}

	private Journalpost.DokumentInfo mapDokument(SafJournalpostTo.DokumentInfo dokumentInfo) {
		return Journalpost.DokumentInfo.builder()
				.tittel(dokumentInfo.getTittel())
				.build();
	}
}
