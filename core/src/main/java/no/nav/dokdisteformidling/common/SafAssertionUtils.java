package no.nav.dokdisteformidling.common;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import no.nav.dokdisteformidling.exception.functional.SafJournalpostValidationException;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
public final class SafAssertionUtils {

	private SafAssertionUtils() {
	}

	public static void assertObjectOnSafJournalpostBodyNotNull(String field, Object objectValue, String journalpostId) {
		if (objectValue == null) {
			throw new SafJournalpostValidationException(format("Feltet %s kan ikke være null i journalpost-respons fra SAF. journalpostId=%s", field, journalpostId));
		}
	}

	public static void assertFieldOnSafJournalpostBodyNotNullOrEmpty(String field, String value, String journalpostId) {
		if (isEmpty(value)) {
			throw new SafJournalpostValidationException(format("Feltet %s kan ikke være null eller tomt i journalpost-respons fra SAF. journalpostId=%s", field, journalpostId));
		}
	}

	public static void assertObjectOnSafDokumenterNotNull(String field, Object objectValue, String journalpostId, String dokumentInfoId) {
		if (objectValue == null) {
			throw new SafJournalpostValidationException(format("Feltet %s kan ikke være null i journalpost-respons fra SAF. journalpostId=%s, dokumentInfoId=%s", field, journalpostId, dokumentInfoId));
		}
	}

	public static void assertFieldOnSafDokumenterNotNullOrEmpty(String field, String value, String journalpostId, String dokumentInfoId) {
		if (isEmpty(value)) {
			throw new SafJournalpostValidationException(format("Feltet %s kan ikke være null eller tomt i journalpost-respons fra SAF. journalpostId=%s, dokumentInfoId=%s", field, journalpostId, dokumentInfoId));
		}
	}
}
