package no.nav.dokdisteformidling.consumer.util;

import static java.lang.String.format;

import no.nav.dokdisteformidling.exception.functional.SafJournalpostValidationException;

public final class ValidationUtil {

	private ValidationUtil() {
	}

	public static void assertDokumentFieldNotNullOrEmpty(String field, String value) {
		if (value == null || value.isEmpty()) {
			throw new SafJournalpostValidationException(format("For dokumenter kan feltet %s ikke være null eller tomt. Fikk %s=%s", field, field, value));
		}
	}
}
