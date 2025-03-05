package no.nav.dokdisteformidling.qdist013;

import no.nav.dokdisteformidling.consumer.ereg.EregConsumer;
import no.nav.dokdisteformidling.consumer.pdl.PdlGraphQLConsumer;
import no.nav.dokdisteformidling.consumer.saf.SafJournalpostQueryService;
import no.nav.dokdisteformidling.qdist013.saf.lightweight.LightweightSafJournalpostQdist013;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static jakarta.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT;
import static no.nav.dokdisteformidling.qdist013.AvtaltmeldingMapperTest.BESTILLINGS_ID;
import static no.nav.dokdisteformidling.qdist013.AvtaltmeldingMapperTest.TEMA;
import static no.nav.dokdisteformidling.qdist013.AvtaltmeldingMapperTest.creatHentPersonInfo;
import static no.nav.dokdisteformidling.qdist013.TestUtil.classpathToString;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.xmlunit.assertj.XmlAssert.assertThat;

public class AvtaltmeldingMarshallerTest {

	private static final Set<String> IGNORE_LOCALNAMES = Set.of("tidspunkt", "tilknyttetDato");

	private final AvtaltmeldingMarshaller avtaltmeldingMarshaller = new AvtaltmeldingMarshaller(Map.of(JAXB_FORMATTED_OUTPUT, true));
	private final EregConsumer eregMock = mock(EregConsumer.class);
	private final SafJournalpostQueryService<LightweightSafJournalpostQdist013> safJournalpostQueryServiceMock = mock(SafJournalpostQueryService.class);
	private final PdlGraphQLConsumer pdlGraphQLConsumer = mock(PdlGraphQLConsumer.class);
	private final AvtaltmeldingMapper avtaltmeldingMapper = new AvtaltmeldingMapper(safJournalpostQueryServiceMock, eregMock, pdlGraphQLConsumer);

	@Test
	void shouldAssertAvtaltmelding() {
		when(pdlGraphQLConsumer.hentPerson(anyString())).thenReturn(creatHentPersonInfo());
		String avtaltmeldingXmlString = avtaltmeldingMarshaller.marshal(avtaltmeldingMapper.createArkivMelding(AvtaltmeldingMapperTest.createJournalpostQdist013Builder()
				.tema(TEMA)
				.build(), BESTILLINGS_ID));

		assertThat(avtaltmeldingXmlString)
				.and(classpathToString("avtaltmelding/avtaltmelding_v1.xml"))
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
