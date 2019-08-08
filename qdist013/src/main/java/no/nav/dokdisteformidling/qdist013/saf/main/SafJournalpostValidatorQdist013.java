package no.nav.dokdisteformidling.qdist013.saf.main;

import static java.lang.String.format;
import static no.nav.dokdisteformidling.common.SafAssertionUtils.assertFieldOnSafDokumenterNotNullOrEmpty;
import static no.nav.dokdisteformidling.common.SafAssertionUtils.assertFieldOnSafJournalpostBodyNotNullOrEmpty;
import static no.nav.dokdisteformidling.common.SafAssertionUtils.assertObjectOnSafDokumenterNotNull;
import static no.nav.dokdisteformidling.common.SafAssertionUtils.assertObjectOnSafJournalpostBodyNotNull;
import static no.nav.dokdisteformidling.qdist013.saf.main.JournalpostQdist013.Datotype.DATO_JOURNALFOERT;

import no.nav.dokdisteformidling.consumer.saf.journalpost.SafJournalpost;
import no.nav.dokdisteformidling.exception.functional.SafJournalpostValidationException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SafJournalpostValidatorQdist013 {

	public void validate(SafJournalpost safJournalpost, String journalpostId) {
		assertJournalpostBody(safJournalpost, journalpostId);
		assertSak(safJournalpost, journalpostId);
		assertBruker(safJournalpost, journalpostId);
		assertDokumenter(safJournalpost.getDokumenter(), journalpostId);
	}

	private void assertJournalpostBody(SafJournalpost safJournalpost, String journalpostId) {
		assertFieldOnSafJournalpostBodyNotNullOrEmpty("journalpost.opprettetAvNavn", safJournalpost.getOpprettetAvNavn(), journalpostId);
		assertObjectOnSafJournalpostBodyNotNull("jounalpost.datoOpprettet", safJournalpost.getDatoOpprettet(), journalpostId);
		assertFieldOnSafJournalpostBodyNotNullOrEmpty("jounalpost.tittel", safJournalpost.getTittel(), journalpostId);
		assertFieldOnSafJournalpostBodyNotNullOrEmpty("jounalpost.kategori", safJournalpost.getKategori(), journalpostId);
		assertFieldOnSafJournalpostBodyNotNullOrEmpty("jounalpost.journalfortAvNavn", safJournalpost.getJournalfortAvNavn(), journalpostId);
		assertFieldOnSafJournalpostBodyNotNullOrEmpty("jounalpost.temanavn", safJournalpost.getTemanavn(), journalpostId);
		assertFieldOnSafJournalpostBodyNotNullOrEmpty("jounalpost.journalposttype", safJournalpost.getJournalposttype(), journalpostId);
		assertThatRelevanteDatoerContainsDatoJournalfoert(safJournalpost, journalpostId);

	}

	private void assertBruker(SafJournalpost safJournalpost, String journalpostId) {
		assertObjectOnSafJournalpostBodyNotNull("jounalpost.bruker", safJournalpost.getBruker(), journalpostId);
		assertFieldOnSafJournalpostBodyNotNullOrEmpty("jounalpost.bruker.id", safJournalpost.getBruker()
				.getId(), journalpostId);
		assertFieldOnSafJournalpostBodyNotNullOrEmpty("jounalpost.bruker.type", safJournalpost.getBruker()
				.getType(), journalpostId);
	}

	private void assertSak(SafJournalpost safJournalpost, String journalpostId) {
		assertObjectOnSafJournalpostBodyNotNull("jounalpost.sak", safJournalpost.getSak(), journalpostId);
		assertObjectOnSafJournalpostBodyNotNull("journalpost.sak.datoOpprettet", safJournalpost.getSak(), journalpostId);
	}

	private void assertThatRelevanteDatoerContainsDatoJournalfoert(SafJournalpost safJournalpost, String journalpostId) {
		safJournalpost.getRelevanteDatoer().stream()
				.filter(relevantDato -> DATO_JOURNALFOERT.name().equals(relevantDato.getDatotype()))
				.findAny()
				.orElseThrow(() -> new SafJournalpostValidationException(format("Feltet journalpost.relevanteDatoer må inneholde DATO_JOURNALFOERT i journalpost-respons fra SAF. journalpostId=%s", journalpostId)));
	}

	private void assertDokumenter(List<SafJournalpost.DokumentInfo> dokumenter, String journalpostId) {
		dokumenter.stream().forEach(dokumentInfo -> {
					assertFieldOnSafDokumenterNotNullOrEmpty("dokumentInfo.dokumentInfoid", dokumentInfo.getDokumentInfoId(), journalpostId, dokumentInfo
							.getDokumentInfoId());
					assertFieldOnSafDokumenterNotNullOrEmpty("dokumentInfo.tittel", dokumentInfo.getTittel(), journalpostId, dokumentInfo
							.getDokumentInfoId());
					assertObjectOnSafDokumenterNotNull("dokumentInfo.datoFerdigstilt", dokumentInfo.getDatoFerdigstilt(), journalpostId, dokumentInfo
							.getDokumentInfoId());
				}
		);
	}

}
