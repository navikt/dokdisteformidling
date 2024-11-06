package no.nav.dokdisteformidling.qdist013.saf.main;

import org.junit.jupiter.api.Test;

import java.util.List;

import static no.nav.dokdisteformidling.qdist013.saf.main.JournalpostQdist013.Datotype.DATO_JOURNALFOERT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class JournalpostQdist013MapperTest extends SafJournalpostTest {

	private final JournalpostQdist013Mapper journalpostQdist013Mapper = new JournalpostQdist013Mapper();

	@Test
	void shouldMap() {
		JournalpostQdist013 journalpostQdist013 = journalpostQdist013Mapper.map(createSafJournalpost().build());
		assertJournalpostQdist013(journalpostQdist013);
	}

	private void assertJournalpostQdist013(JournalpostQdist013 journalpostQdist013) {
		assertNotNull(journalpostQdist013);
		assertBruker(journalpostQdist013.getBruker());
		assertEquals(OPPRETTET_AV_NAVN, journalpostQdist013.getOpprettetAvNavn());
		assertDokumenter(journalpostQdist013.getDokumenter());
		assertEquals(JOURNALFORT_AV_NAVN, journalpostQdist013.getJournalfortAvNavn());
		assertEquals(JOURNALPOST_ID, journalpostQdist013.getJournalpostId());
		assertEquals(JOURNALPOST_TYPE, journalpostQdist013.getJournalposttype());
		assertSak(journalpostQdist013.getSak());
		assertEquals(DATO_OPPRETTET_JP, journalpostQdist013.getDatoOpprettet());
		assertEquals(JOURNALFOERENDEENHET, journalpostQdist013.getJournalfoerendeEnhet());
		assertEquals(TEMANAVN, journalpostQdist013.getTemanavn());
		assertEquals(TITTEL, journalpostQdist013.getTittel());
		assertRelevanteDatoer(journalpostQdist013.getRelevanteDatoer());
		assertEquals(JOURNALFOERT_DATO, journalpostQdist013.getDatoJournalfoert());
	}

	private void assertBruker(JournalpostQdist013.Bruker bruker) {
		assertNotNull(bruker);
		assertEquals(BRUKER_ID, bruker.getId());
		assertEquals(BRUKER_TYPE, bruker.getType());
	}

	private void assertSak(JournalpostQdist013.Sak sak) {
		assertNotNull(sak);
		assertEquals(DATO_OPPRETTET_SAK, sak.getDatoOpprettet());
	}

	private void assertDokumenter(List<JournalpostQdist013.DokumentInfo> dokumentInfos) {
		assertNotNull(dokumentInfos);
		assertHovedDokumentInfo(dokumentInfos.get(0));
		assertVedleggDokumentInfo(dokumentInfos.get(1));
	}

	private void assertHovedDokumentInfo(JournalpostQdist013.DokumentInfo hovedDokumentInfo) {
		assertNotNull(hovedDokumentInfo);
		assertNull(hovedDokumentInfo.getOriginalJournalpostId());
		assertHovedDokumentVarianter(hovedDokumentInfo.getDokumentvarianter());
		assertEquals(HOVEDDOK_TITTEL, hovedDokumentInfo.getTittel());
		assertEquals(HOVEDDOK_DOKUMENT_INFO_ID, hovedDokumentInfo.getDokumentInfoId());
	}

	private void assertVedleggDokumentInfo(JournalpostQdist013.DokumentInfo hovedDokumentInfo) {
		assertNotNull(hovedDokumentInfo);
		assertEquals(ORIGINAL_JOURNALPOST_ID, hovedDokumentInfo.getOriginalJournalpostId());
		assertEquals(1, hovedDokumentInfo.getDokumentvarianter().size());
		assertDokumentVariantPDF(hovedDokumentInfo.getDokumentvarianter().getFirst());
		assertEquals(VEDLEGG_TITTEL, hovedDokumentInfo.getTittel());
		assertEquals(VEDLEGG_DOKUMENT_INFO_ID, hovedDokumentInfo.getDokumentInfoId());
	}

	private void assertHovedDokumentVarianter(List<JournalpostQdist013.DokumentInfo.Dokumentvariant> dokumentvarianter) {
		assertNotNull(dokumentvarianter);
		assertEquals(2, dokumentvarianter.size());
		assertDokumentVariantXML(dokumentvarianter.get(0));
		assertDokumentVariantPDF(dokumentvarianter.get(1));
	}

	private void assertDokumentVariantXML(JournalpostQdist013.DokumentInfo.Dokumentvariant dokumentvariant) {
		assertNotNull(dokumentvariant);
		assertEquals(VARIANTFORMAT_ORIGINAL, dokumentvariant.getVariantformat());
		assertEquals(FILTYPE_XML, dokumentvariant.getFiltype());
	}

	private void assertDokumentVariantPDF(JournalpostQdist013.DokumentInfo.Dokumentvariant dokumentvariant) {
		assertNotNull(dokumentvariant);
		assertEquals(VARIANTFORMAT_ARKIV, dokumentvariant.getVariantformat());
		assertEquals(FILTYPE_PDF, dokumentvariant.getFiltype());
	}

	private void assertRelevanteDatoer(List<JournalpostQdist013.RelevantDato> relevantDatoer) {
		assertNotNull(relevantDatoer);
		assertEquals(1, relevantDatoer.size());
		assertRelevantDatoJournalfoert(relevantDatoer.getFirst());
	}

	private void assertRelevantDatoJournalfoert(JournalpostQdist013.RelevantDato relevantDato) {
		assertNotNull(relevantDato);
		assertEquals(JOURNALFOERT_DATO, relevantDato.getDato());
		assertEquals(DATO_JOURNALFOERT, relevantDato.getDatotype());
	}


}
