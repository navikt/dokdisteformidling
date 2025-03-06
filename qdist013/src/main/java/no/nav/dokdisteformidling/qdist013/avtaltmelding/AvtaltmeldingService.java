package no.nav.dokdisteformidling.qdist013.avtaltmelding;

import no.nav.dokdisteformidling.qdist013.saf.main.JournalpostQdist013;

public interface AvtaltmeldingService {
	Avtaltmelding map(JournalpostQdist013 journalpostQdist013, String bestillingsId);
}
