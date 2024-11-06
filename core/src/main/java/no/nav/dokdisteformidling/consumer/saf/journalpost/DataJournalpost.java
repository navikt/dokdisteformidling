package no.nav.dokdisteformidling.consumer.saf.journalpost;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class DataJournalpost implements Serializable {

	private SafJournalpost journalpost;
}