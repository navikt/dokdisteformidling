package no.nav.dokdisteformidling.consumer.saf.graphql;

import static no.nav.dokdisteformidling.consumer.util.ValidationUtil.assertDokumentFieldNotNullOrEmpty;

import no.nav.dokdisteformidling.consumer.saf.journalpost.SafJournalpostTo;

import java.util.List;

public class JournalpostToValidator {

	public SafJournalpostTo validateAndReturn(SafJournalpostTo safJournalpostTo) {

		validateDokumenter(safJournalpostTo.getDokumenter());

		return safJournalpostTo;
	}

	private void validateDokumenter(List<SafJournalpostTo.DokumentInfo> dokumenter) {
		dokumenter.forEach(this::validateDokument);
	}

	private void validateDokument(SafJournalpostTo.DokumentInfo dokumentInfo) {
		assertDokumentFieldNotNullOrEmpty("tittel", dokumentInfo.getTittel());
		assertDokumentFieldNotNullOrEmpty("dokumentInfoId", dokumentInfo.getDokumentInfoId());
	}
}
