package no.nav.dokdisteformidling.exception.functional;

import no.nav.dokdisteformidling.exception.technical.SafJournalpostQueryTechnicalException;

/**
 * Journalpost finnes ikke ved query mot saf.
 *
 * Dette er klassifisert som en teknisk (midlertidig) feil som kan løses med retry eller rekjøring på backoutkø.
 * Det er ikke en forventet funksjonell feil at journalposten ikke finnes i joark når det har kommet så langt i verdikjeden.
 */
public class SafJournalpostIkkeFunnetException extends SafJournalpostQueryTechnicalException {
	public SafJournalpostIkkeFunnetException(String message) {
		super(message);
	}
}
