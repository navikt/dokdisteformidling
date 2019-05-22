package no.nav.dokdisteformidling.consumer.util;

import static java.lang.String.format;

import no.nav.dokdisteformidling.exception.functional.ValidationException;

public final class ValidationUtil {

	private ValidationUtil() {
	}

	public static void assertJournalpostFieldNotNull(Class inputClass, Object value) {
		if (value == null) {
			throw new ValidationException(format("For journalposter kan feltet %s ikke være null eller tomt. Fikk %s=null", inputClass.getCanonicalName(), inputClass.getCanonicalName()));
		}
	}

	public static void assertDokumentFieldNotNullOrEmpty(String field, String value) {
		if (value == null || value.isEmpty()) {
			throw new ValidationException(format("For dokumenter kan feltet %s ikke være null eller tomt. Fikk %s=%s", field, field, value));
		}
	}
}
