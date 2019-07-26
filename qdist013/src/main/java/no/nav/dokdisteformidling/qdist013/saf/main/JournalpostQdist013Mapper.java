package no.nav.dokdisteformidling.qdist013.saf.main;

import static no.nav.dokdisteformidling.qdist013.saf.main.JournalpostQdist013.Datotype.DATO_JOURNALFOERT;

import no.nav.dokdisteformidling.consumer.saf.journalpost.SafJournalpost;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

@Component
public class JournalpostQdist013Mapper {

	public JournalpostQdist013 map(SafJournalpost safJournalpost) {

		return JournalpostQdist013.builder()
				.sak(JournalpostQdist013.Sak.builder()
						.datoOpprettet(safJournalpost.getDatoOpprettet())
						.build())
				.opprettetAvNavn(safJournalpost.getOpprettetAvNavn())
				.bruker(JournalpostQdist013.Bruker.builder()
						.id(safJournalpost.getBruker().getId())
						.type(safJournalpost.getBruker().getType())
						.build())
				.datoOpprettet(safJournalpost.getDatoOpprettet())
				.tittel(safJournalpost.getTittel())
				.kategori(safJournalpost.getKategori())
				.journalfortAvNavn(safJournalpost.getJournalfortAvNavn())
				.temanavn(safJournalpost.getTemanavn())
				.relevanteDatoer(Arrays.asList(safJournalpost.getRelevanteDatoer().stream()
						.filter(relevantDato -> DATO_JOURNALFOERT.name().equals(relevantDato.getDatotype()))
						.map(relevantDato ->
								JournalpostQdist013.RelevantDato.builder()
										.dato(relevantDato.getDato())
										.datotype(DATO_JOURNALFOERT)
										.build()
						)
						.findAny()
						.get())) //This is Ok. Validation is done prior to mapping
				.dokumenter(safJournalpost.getDokumenter().stream()
						.map(dokumentInfo -> JournalpostQdist013.DokumentInfo.builder()
								.dokumentInfoId(dokumentInfo.getDokumentInfoId())
								.tittel(dokumentInfo.getTittel())
								.datoFerdigstilt(dokumentInfo.getDatoFerdigstilt())
								.originalJournalpostId(dokumentInfo.getOriginalJournalpostId())
								.build())
						.collect(Collectors.toList()))
				.build();
	}
}
