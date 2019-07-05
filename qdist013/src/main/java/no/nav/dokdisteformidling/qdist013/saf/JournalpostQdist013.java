package no.nav.dokdisteformidling.qdist013.saf;

import lombok.Builder;
import lombok.Value;
import no.nav.dokdisteformidling.consumer.saf.journalpost.Journalpost;

import java.util.ArrayList;
import java.util.List;

@Value
@Builder
public class JournalpostQdist013 implements Journalpost {
	//TODO Legg til de feltene vi trenger

	@Builder.Default
	private final List<DokumentInfo> dokumenter = new ArrayList<>();

	@Value
	@Builder
	public static class DokumentInfo {
		private final String dokumentInfoId;
		private final String tittel;
	}
}
