package no.nav.dokdisteformidling.qdist013.saf;

import no.nav.dokdisteformidling.consumer.saf.journalpost.SafJournalpost;
import org.springframework.stereotype.Component;

@Component
public class JournalpostQdist013Mapper {

	public JournalpostQdist013 map(SafJournalpost safJournalpost) {

		//TODO Map attributter!
		return JournalpostQdist013.builder()
				.build();
	}
}
