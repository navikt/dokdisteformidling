package no.nav.dokdisteformidling.qdist011.saf;

import no.nav.dokdisteformidling.consumer.saf.journalpost.SafJournalpost;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class JournalpostQdist011Mapper {

	public JournalpostQdist011 map(SafJournalpost safJournalpost) {

		return JournalpostQdist011.builder()
				.dokumenter(safJournalpost.getDokumenter()
						.stream()
						.map(dokumentInfo -> JournalpostQdist011.DokumentInfo.builder()
								.dokumentInfoId(dokumentInfo.getDokumentInfoId())
								.tittel(dokumentInfo.getTittel())
								.build())
						.collect(Collectors.toList()))
				.build();
	}
}
