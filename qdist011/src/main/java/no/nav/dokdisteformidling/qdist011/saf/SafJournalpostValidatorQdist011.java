package no.nav.dokdisteformidling.qdist011.saf;

import static no.nav.dokdisteformidling.consumer.util.ValidationUtil.assertDokumentFieldNotNullOrEmpty;

import no.nav.dokdisteformidling.consumer.saf.journalpost.SafJournalpost;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SafJournalpostValidatorQdist011 {

	public void validate(SafJournalpost safJournalpost) {
		validateDokumenter(safJournalpost.getDokumenter());
	}

	private void validateDokumenter(List<SafJournalpost.DokumentInfo> dokumenter) {
		dokumenter.forEach(this::validateDokument);
	}

	private void validateDokument(SafJournalpost.DokumentInfo dokumentInfo) {
		assertDokumentFieldNotNullOrEmpty("tittel", dokumentInfo.getTittel());
		assertDokumentFieldNotNullOrEmpty("dokumentInfoId", dokumentInfo.getDokumentInfoId());
	}
}
