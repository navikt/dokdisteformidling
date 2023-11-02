package no.nav.dokdisteformidling.qdist013.saf.main;

import no.nav.dokdisteformidling.consumer.saf.journalpost.SafJournalpost;
import org.springframework.stereotype.Component;

import java.util.List;

import static no.nav.dokdisteformidling.qdist013.saf.main.JournalpostQdist013.Datotype.DATO_JOURNALFOERT;

@Component
public class JournalpostQdist013Mapper {

	public JournalpostQdist013 map(SafJournalpost safJournalpost) {

		return JournalpostQdist013.builder()
				.journalpostId(safJournalpost.getJournalpostId())
				.sak(JournalpostQdist013.Sak.builder()
						.arkivsaksnummer(safJournalpost.getSak().getArkivsaksnummer())
						.datoOpprettet(safJournalpost.getSak().getDatoOpprettet())
						.build())
				.opprettetAvNavn(safJournalpost.getOpprettetAvNavn())
				.bruker(JournalpostQdist013.Bruker.builder()
						.id(safJournalpost.getBruker().getId())
						.type(safJournalpost.getBruker().getType())
						.build())
				.datoOpprettet(safJournalpost.getDatoOpprettet())
				.tittel(safJournalpost.getTittel())
				.journalfortAvNavn(safJournalpost.getJournalfortAvNavn())
				.temanavn(safJournalpost.getTemanavn())
				.tema(safJournalpost.getTema())
				.relevanteDatoer(List.of(safJournalpost.getRelevanteDatoer().stream()
						.filter(relevantDato -> DATO_JOURNALFOERT.name().equals(relevantDato.getDatotype()))
						.map(relevantDato ->
								JournalpostQdist013.RelevantDato.builder()
										.dato(relevantDato.getDato())
										.datotype(DATO_JOURNALFOERT)
										.build()
						)
						.findAny()
						.get())) //This is Ok. Validation is done prior to mapping
				.journalposttype(safJournalpost.getJournalposttype())
				.journalfoerendeEnhet(safJournalpost.getJournalfoerendeEnhet())
				.dokumenter(safJournalpost.getDokumenter().stream()
						.map(dokumentInfo -> JournalpostQdist013.DokumentInfo.builder()
								.dokumentInfoId(dokumentInfo.getDokumentInfoId())
								.dokumentstatus(dokumentInfo.getDokumentstatus())
								.tittel(dokumentInfo.getTittel())
								.originalJournalpostId(dokumentInfo.getOriginalJournalpostId())
								.dokumentvarianter(dokumentInfo.getDokumentvarianter().stream()
										.map(dokumentvariant -> JournalpostQdist013.DokumentInfo.Dokumentvariant.builder()
												.filtype(dokumentvariant.getFiltype())
												.variantformat(dokumentvariant.getVariantformat())
												.build())
										.toList())
								.build())
						.toList())
				.build();
	}
}
