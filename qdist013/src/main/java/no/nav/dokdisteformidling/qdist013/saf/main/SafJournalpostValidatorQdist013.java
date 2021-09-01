package no.nav.dokdisteformidling.qdist013.saf.main;

import no.nav.dokdisteformidling.consumer.saf.journalpost.SafJournalpost;
import no.nav.dokdisteformidling.exception.functional.SafJournalpostValidationException;
import org.springframework.stereotype.Component;

import java.util.List;

import static java.lang.String.format;
import static no.nav.dokdisteformidling.constants.DomainConstants.VARIANTFORMAT_ARKIV;
import static no.nav.dokdisteformidling.constants.DomainConstants.VARIANTFORMAT_SLADDET;
import static no.nav.dokdisteformidling.qdist013.saf.main.JournalpostQdist013.Datotype.DATO_JOURNALFOERT;
import static no.nav.dokdisteformidling.utils.SafAssertionUtils.assertFieldOnSafDokumenterNotNullOrEmpty;
import static no.nav.dokdisteformidling.utils.SafAssertionUtils.assertFieldOnSafJournalpostBodyNotNullOrEmpty;
import static no.nav.dokdisteformidling.utils.SafAssertionUtils.assertObjectOnSafDokumenterNotNull;
import static no.nav.dokdisteformidling.utils.SafAssertionUtils.assertObjectOnSafJournalpostBodyNotNull;

@Component
public class SafJournalpostValidatorQdist013 {

	public void validate(SafJournalpost safJournalpost, String journalpostId) {
		assertJournalpostBody(safJournalpost, journalpostId);
		assertSak(safJournalpost, journalpostId);
		assertBruker(safJournalpost, journalpostId);
		assertDokumenter(safJournalpost.getDokumenter(), journalpostId);
	}

	private void assertJournalpostBody(SafJournalpost safJournalpost, String journalpostId) {
		assertFieldOnSafJournalpostBodyNotNullOrEmpty("journalpost.journalpostId", safJournalpost.getJournalpostId(), journalpostId);
		assertFieldOnSafJournalpostBodyNotNullOrEmpty("journalpost.opprettetAvNavn", safJournalpost.getOpprettetAvNavn(), journalpostId);
		assertObjectOnSafJournalpostBodyNotNull("jounalpost.datoOpprettet", safJournalpost.getDatoOpprettet(), journalpostId);
		assertFieldOnSafJournalpostBodyNotNullOrEmpty("jounalpost.tittel", safJournalpost.getTittel(), journalpostId);
		assertFieldOnSafJournalpostBodyNotNullOrEmpty("jounalpost.journalfortAvNavn", safJournalpost.getJournalfortAvNavn(), journalpostId);
		assertFieldOnSafJournalpostBodyNotNullOrEmpty("jounalpost.temanavn", safJournalpost.getTemanavn(), journalpostId);
		assertFieldOnSafJournalpostBodyNotNullOrEmpty("jounalpost.tema", safJournalpost.getTema(), journalpostId);
		assertFieldOnSafJournalpostBodyNotNullOrEmpty("jounalpost.journalposttype", safJournalpost.getJournalposttype(), journalpostId);
		assertFieldOnSafJournalpostBodyNotNullOrEmpty("jounalpost.journalfoerendeEnhet", safJournalpost.getJournalfoerendeEnhet(), journalpostId);
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
		assertObjectOnSafJournalpostBodyNotNull("journalpost.sak.arkivsaksnummer", safJournalpost.getSak().getArkivsaksnummer(), journalpostId);
		assertObjectOnSafJournalpostBodyNotNull("journalpost.sak.datoOpprettet", safJournalpost.getSak().getDatoOpprettet(), journalpostId);
	}

	private void assertThatRelevanteDatoerContainsDatoJournalfoert(SafJournalpost safJournalpost, String journalpostId) {
		assertObjectOnSafJournalpostBodyNotNull("jounalpost.relevanteDatoer", safJournalpost.getRelevanteDatoer(), journalpostId);
		safJournalpost.getRelevanteDatoer().stream()
				.filter(relevantDato -> DATO_JOURNALFOERT.name().equals(relevantDato.getDatotype()))
				.findAny()
				.orElseThrow(() -> new SafJournalpostValidationException(format("Feltet journalpost.relevanteDatoer må inneholde DATO_JOURNALFOERT i journalpost-respons fra SAF. journalpostId=%s", journalpostId)));
	}

	private void assertDokumenter(List<SafJournalpost.DokumentInfo> dokumenter, String journalpostId) {
		assertObjectOnSafJournalpostBodyNotNull("jounalpost.dokumenter", dokumenter, journalpostId);
		dokumenter.forEach(dokumentInfo -> {
					assertFieldOnSafDokumenterNotNullOrEmpty("dokumentInfo.dokumentInfoid", dokumentInfo.getDokumentInfoId(), journalpostId, dokumentInfo
							.getDokumentInfoId());
					assertFieldOnSafDokumenterNotNullOrEmpty("dokumentInfo.tittel", dokumentInfo.getTittel(), journalpostId, dokumentInfo
							.getDokumentInfoId());
					assertObjectOnSafDokumenterNotNull("dokumentInfo.dokumentvarianter", dokumentInfo.getDokumentvarianter(), journalpostId, dokumentInfo
							.getDokumentInfoId());
					if (!dokumentInfoContainsDokumentvariantSladdetOrArkiv(dokumentInfo)) {
						throw new SafJournalpostValidationException(format("DokumentInfo-objekt med dokumentInfoId=%s har ikke tilknyttede dokumentvarianter ARKIV eller SLADDET. journalpostId=%s.", dokumentInfo
								.getDokumentInfoId(), journalpostId));

					}
				}
		);
	}

	private boolean dokumentInfoContainsDokumentvariantSladdetOrArkiv(SafJournalpost.DokumentInfo dokumentInfo) {
		return dokumentInfo.getDokumentvarianter()
				.stream()
				.anyMatch(dokumentvariant -> (VARIANTFORMAT_SLADDET.equals(dokumentvariant.getVariantformat())
						|| VARIANTFORMAT_ARKIV.equals(dokumentvariant.getVariantformat())));
	}

}
