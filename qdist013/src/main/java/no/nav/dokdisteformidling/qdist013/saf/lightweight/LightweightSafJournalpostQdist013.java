package no.nav.dokdisteformidling.qdist013.saf.lightweight;

import lombok.Builder;
import lombok.Value;
import no.nav.dokdisteformidling.consumer.saf.journalpost.Journalpost;

@Value
@Builder
public class LightweightSafJournalpostQdist013 implements Journalpost {

	private final String journalfortAvNavn;

}
