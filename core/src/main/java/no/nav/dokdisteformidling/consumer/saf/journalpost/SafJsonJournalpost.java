package no.nav.dokdisteformidling.consumer.saf.journalpost;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class SafJsonJournalpost implements Serializable {

	private DataJournalpost data;

	public SafJournalpost getJournalpost() {
		return data.getJournalpost();
	}
}
