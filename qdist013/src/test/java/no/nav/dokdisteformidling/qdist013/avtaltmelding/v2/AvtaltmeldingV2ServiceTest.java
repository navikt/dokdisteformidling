package no.nav.dokdisteformidling.qdist013.avtaltmelding.v2;

import no.nav.dokdisteformidling.consumer.ereg.EregConsumer;
import no.nav.dokdisteformidling.consumer.pdl.PdlGraphQLConsumer;
import no.nav.dokdisteformidling.consumer.saf.SafJournalpostQueryService;
import no.nav.dokdisteformidling.qdist013.avtaltmelding.Avtaltmelding;
import no.nav.dokdisteformidling.qdist013.saf.lightweight.LightweightSafJournalpostQdist013;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static jakarta.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT;
import static no.nav.dokdisteformidling.qdist013.avtaltmelding.v2.AvtaltmeldingV2MapperTest.BESTILLINGS_ID;
import static no.nav.dokdisteformidling.qdist013.avtaltmelding.v2.AvtaltmeldingV2MapperTest.DOKUMENT_INFO_ID_HOVEDDOK;
import static no.nav.dokdisteformidling.qdist013.avtaltmelding.v2.AvtaltmeldingV2MapperTest.DOKUMENT_INFO_ID_VEDLEGG;
import static no.nav.dokdisteformidling.qdist013.avtaltmelding.v2.AvtaltmeldingV2MapperTest.TEMA;
import static no.nav.dokdisteformidling.qdist013.avtaltmelding.v2.AvtaltmeldingV2MapperTest.creatHentPersonInfo;
import static no.nav.dokdisteformidling.qdist013.avtaltmelding.v2.AvtaltmeldingV2MapperTest.createJournalpostQdist013Builder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AvtaltmeldingV2ServiceTest {

	private final EregConsumer eregMock = mock(EregConsumer.class);
	private final SafJournalpostQueryService<LightweightSafJournalpostQdist013> safJournalpostQueryServiceMock = mock(SafJournalpostQueryService.class);
	private final PdlGraphQLConsumer pdlGraphQLConsumer = mock(PdlGraphQLConsumer.class);
	private final AvtaltmeldingV2Marshaller avtaltmeldingV2Marshaller = new AvtaltmeldingV2Marshaller(Map.of(JAXB_FORMATTED_OUTPUT, true));
	private final AvtaltmeldingV2Mapper avtaltmeldingV2Mapper = new AvtaltmeldingV2Mapper(safJournalpostQueryServiceMock, eregMock, pdlGraphQLConsumer);
	private final AvtaltmeldingV2Service avtaltmeldingV2Service = new AvtaltmeldingV2Service(avtaltmeldingV2Mapper, avtaltmeldingV2Marshaller);

	@Test
	void shouldMap() {
		when(pdlGraphQLConsumer.hentPerson(anyString())).thenReturn(creatHentPersonInfo());

		Avtaltmelding avtaltmelding = avtaltmeldingV2Service.map(createJournalpostQdist013Builder()
				.tema(TEMA)
				.build(), BESTILLINGS_ID);

		assertThat(avtaltmelding.asXmlString()).isNotEmpty();
		assertThat(avtaltmelding.asBytes()).isNotEmpty();
		assertThat(avtaltmelding.lookupFilnavn(DOKUMENT_INFO_ID_HOVEDDOK))
				.isEqualTo("987654321-1234567-Dokument hvor deler av innholdet er skjermet.pdf");
		assertThat(avtaltmelding.lookupFilnavn(DOKUMENT_INFO_ID_VEDLEGG))
				.isEqualTo("987654321-7654321-Arkivformat.jpeg");
	}
}