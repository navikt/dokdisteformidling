package no.nav.dokdisteformidling.consumer.saf;

import no.nav.dokdisteformidling.consumer.saf.journalpost.SafJournalpostTo;

public interface SafJournalpostQueryService {

	SafJournalpostTo hentJournalpost(String journalpostid);

}
