package no.nav.dokdisteformidling.qdist013.util;

import no.nav.dokdisteformidling.qdist013.saf.JournalpostQdist013;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
public final class ArkivMapperUtil {

	private static final String AKTOERID = "AKTOERID";
	private static final String ORGNR = "ORGNR";

	private ArkivMapperUtil() {
	}

	public static boolean brukerTypeIsAktoerId(JournalpostQdist013 journalpostQdist013) {
		return AKTOERID.equals(journalpostQdist013.getBruker().getType());
	}

	public static boolean brukerTypeIsOrgnr(JournalpostQdist013 journalpostQdist013) {
		return ORGNR.equals(journalpostQdist013.getBruker().getType());
	}

	public static boolean isHoveddokument(int rekkefolge) {
		return rekkefolge == 0;
	}

}
