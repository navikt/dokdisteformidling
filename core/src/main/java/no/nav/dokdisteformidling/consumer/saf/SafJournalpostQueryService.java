package no.nav.dokdisteformidling.consumer.saf;

import no.nav.dokdisteformidling.consumer.saf.journalpost.Journalpost;

public interface SafJournalpostQueryService<T extends Journalpost> {

	T hentJournalpost(String journalpostid);

}
