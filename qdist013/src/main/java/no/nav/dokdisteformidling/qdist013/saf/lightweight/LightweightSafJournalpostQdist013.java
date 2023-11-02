package no.nav.dokdisteformidling.qdist013.saf.lightweight;

import lombok.Builder;
import lombok.Value;
import no.nav.dokdisteformidling.consumer.saf.journalpost.Journalpost;

import java.time.LocalDateTime;

@Value
@Builder
public class LightweightSafJournalpostQdist013 implements Journalpost {

	String journalfortAvNavn;
	String journalposttype;
	String avsenderMottakerNavn;
	LocalDateTime datoJournalfoert;

}
