package no.nav.dokdisteformidling.qdist013.avtaltmelding.v2;

import no.nav.dokdisteformidling.consumer.ereg.EregConsumer;
import no.nav.dokdisteformidling.consumer.pdl.PdlGraphQLConsumer;
import no.nav.dokdisteformidling.consumer.saf.SafJournalpostQueryService;
import no.nav.dokdisteformidling.qdist013.saf.lightweight.LightweightSafJournalpostQdist013;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static jakarta.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT;
import static no.nav.dokdisteformidling.qdist013.TestUtil.classpathToString;
import static no.nav.dokdisteformidling.qdist013.avtaltmelding.v2.AvtaltmeldingV2MapperTest.BESTILLINGS_ID;
import static no.nav.dokdisteformidling.qdist013.avtaltmelding.v2.AvtaltmeldingV2MapperTest.TEMA;
import static no.nav.dokdisteformidling.qdist013.avtaltmelding.v2.AvtaltmeldingV2MapperTest.creatHentPersonInfo;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.xmlunit.assertj.XmlAssert.assertThat;

public class AvtaltmeldingV2MarshallerTest {

	private static final Set<String> IGNORE_LOCALNAMES = Set.of("tidspunkt", "tilknyttetDato");

	private final AvtaltmeldingV2Marshaller avtaltmeldingV2Marshaller = new AvtaltmeldingV2Marshaller(Map.of(JAXB_FORMATTED_OUTPUT, true));
	private final EregConsumer eregMock = mock(EregConsumer.class);
	private final SafJournalpostQueryService<LightweightSafJournalpostQdist013> safJournalpostQueryServiceMock = mock(SafJournalpostQueryService.class);
	private final PdlGraphQLConsumer pdlGraphQLConsumer = mock(PdlGraphQLConsumer.class);
	private final AvtaltmeldingV2Mapper avtaltmeldingV2Mapper = new AvtaltmeldingV2Mapper(safJournalpostQueryServiceMock, eregMock, pdlGraphQLConsumer);

	@Test
	void shouldAssertAvtaltmelding() {
		when(pdlGraphQLConsumer.hentPerson(anyString())).thenReturn(creatHentPersonInfo());
		String avtaltmeldingXmlString = avtaltmeldingV2Marshaller.marshal(avtaltmeldingV2Mapper.createArkivMelding(AvtaltmeldingV2MapperTest.createJournalpostQdist013Builder()
				.tema(TEMA)
				.build(), BESTILLINGS_ID));

		System.out.println(avtaltmeldingXmlString);
		assertThat(avtaltmeldingXmlString)
				.and(classpathToString("avtaltmelding/avtaltmelding_v2.xml"))
				.ignoreWhitespace()
				.withNodeFilter(node -> {
					if(node.getLocalName() != null) {
						return !IGNORE_LOCALNAMES.contains(node.getLocalName());
					}
					return true;
				})
				.areIdentical();
	}
}
