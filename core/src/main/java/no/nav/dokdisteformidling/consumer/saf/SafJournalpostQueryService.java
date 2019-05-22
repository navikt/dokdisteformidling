package no.nav.dokdisteformidling.consumer.saf;

import no.nav.dokdisteformidling.consumer.saf.journalpost.Journalpost;

public interface SafJournalpostQueryService {

	Journalpost hentJournalpost(String journalpostid);

}
