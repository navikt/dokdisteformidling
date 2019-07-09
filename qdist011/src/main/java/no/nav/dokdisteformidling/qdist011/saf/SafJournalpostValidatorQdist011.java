package no.nav.dokdisteformidling.qdist011.saf;

import static java.lang.String.format;

import no.nav.dokdisteformidling.consumer.saf.journalpost.SafJournalpost;
import no.nav.dokdisteformidling.exception.functional.SafJournalpostValidationException;
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

	public static void assertDokumentFieldNotNullOrEmpty(String field, String value) {
		if (value == null || value.isEmpty()) {
			throw new SafJournalpostValidationException(format("For dokumenter kan feltet %s ikke være null eller tomt. Fikk %s=%s", field, field, value));
		}
	}
}
