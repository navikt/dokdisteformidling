package no.nav.dokdisteformidling.qdist011.saf;

import no.nav.dokdisteformidling.consumer.saf.journalpost.SafJournalpost;
import org.springframework.stereotype.Component;

import static no.nav.dokdisteformidling.utils.SafAssertionUtils.assertFieldOnSafDokumenterNotNullOrEmpty;

@Component
public class SafJournalpostValidatorQdist011 {

	public void validate(SafJournalpost safJournalpost, String journalpostid) {
		safJournalpost.getDokumenter().forEach(dokumentInfo -> validateDokument(dokumentInfo, journalpostid));
	}

	private void validateDokument(SafJournalpost.DokumentInfo dokumentInfo, String journalpostId) {
		assertFieldOnSafDokumenterNotNullOrEmpty("dokumentInfo.tittel", dokumentInfo.getTittel(), journalpostId, dokumentInfo.getDokumentInfoId());
		assertFieldOnSafDokumenterNotNullOrEmpty("dokumentInfo.dokumentInfoId", dokumentInfo.getDokumentInfoId(), journalpostId, dokumentInfo
				.getDokumentInfoId());
	}
}
