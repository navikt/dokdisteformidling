package no.nav.dokdisteformidling.consumer.saf.journalpost;

import java.io.Serializable;

public class DataJournalpost implements Serializable {

	private SafJournalpost journalpost;

	public SafJournalpost getJournalpost() {
		return journalpost;
	}

	public void setJournalpost(SafJournalpost journalpost) {
		this.journalpost = journalpost;
	}
}