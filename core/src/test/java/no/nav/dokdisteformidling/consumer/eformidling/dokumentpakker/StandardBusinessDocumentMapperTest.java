package no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker;

import no.nav.dokdisteformidling.AppTestUtils;
import no.nav.dokdisteformidling.consumer.eformidling.Organisasjonsnummer;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.sbdh.CorrelationInformation;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.sbdh.PartnerIdentification;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.sbdh.Receiver;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.sbdh.Scope;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.sbdh.Sender;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.sbdh.StandardBusinessDocument;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;

import static no.nav.dokdisteformidling.constants.DomainConstants.DEFAULT_ZONE_ID;
import static no.nav.dokdisteformidling.consumer.eformidling.EformidlingConstants.NAV_ORGNUMMER;
import static no.nav.dokdisteformidling.consumer.eformidling.EformidlingConstants.TRYGDERETTEN_ORGNUMMER;
import static no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.StandardBusinessDocumentMapper.AVTALTMELDING_FORRETNINGSMELDING;
import static no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.StandardBusinessDocumentMapper.DOCUMENT_IDENTIFICATOR_STANDARD;
import static no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.StandardBusinessDocumentMapper.HEADER_VERSION;
import static no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.StandardBusinessDocumentMapper.IDENTIFIER_AUTHORITY;
import static no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.StandardBusinessDocumentMapper.SCOPE_CONVERSATION_ID;
import static no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.StandardBusinessDocumentMapper.SCOPE_CONVERSATION_ID_IDENTIFIER;
import static no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.StandardBusinessDocumentMapper.TYPE_VERSION;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
class StandardBusinessDocumentMapperTest {
	private static final String KONVERSASJON_ID = "konversasjonId1";
	private static final String BESTILLINGS_ID = "bestillingsId1";
	private static final String FIXED_TIME = "2020-01-01T12:00:00Z";
	private static final String TEN_SECONDS_BEFORE = "2020-01-01T12:59:50+01:00";
	private static final String TWENTY_FOUR_HOURS_LATER = "2020-01-02T13:00:00+01:00";
	private final StandardBusinessDocumentMapper mapper = new StandardBusinessDocumentMapper(Clock.fixed(Instant.parse(FIXED_TIME), DEFAULT_ZONE_ID));
	private static final String ARKIVEMELDING_XML = AppTestUtils.classpathToString("avtaltmelding/arkivmelding.xml");
	@Test
	void shouldMapArkivmeldingKonvolutt() {
		final StandardBusinessDocument sbd = mapper.mapAvtaltmeldingEnvelope(KONVERSASJON_ID, BESTILLINGS_ID,ARKIVEMELDING_XML);

		assertThat(sbd.getStandardBusinessDocumentHeader().getHeaderVersion()).isEqualTo(HEADER_VERSION);
		assertThat(sbd.getStandardBusinessDocumentHeader().getSender())
				.extracting(Sender::getIdentifier)
				.extracting(PartnerIdentification::getAuthority).contains(IDENTIFIER_AUTHORITY);
		assertThat(sbd.getStandardBusinessDocumentHeader().getSender())
				.extracting(Sender::getIdentifier)
				.extracting(PartnerIdentification::getValue).contains(Organisasjonsnummer.asIso6523(NAV_ORGNUMMER));
		assertThat(sbd.getStandardBusinessDocumentHeader().getReceiver())
				.extracting(Receiver::getIdentifier)
				.extracting(PartnerIdentification::getAuthority).contains(IDENTIFIER_AUTHORITY);
		assertThat(sbd.getStandardBusinessDocumentHeader().getReceiver())
				.extracting(Receiver::getIdentifier)
				.extracting(PartnerIdentification::getValue).contains(Organisasjonsnummer.asIso6523(TRYGDERETTEN_ORGNUMMER));
		assertThat(sbd.getStandardBusinessDocumentHeader().getDocumentIdentification().getStandard()).isEqualTo(DOCUMENT_IDENTIFICATOR_STANDARD);
		assertThat(sbd.getStandardBusinessDocumentHeader().getDocumentIdentification().getTypeVersion()).isEqualTo(TYPE_VERSION);
		assertThat(sbd.getStandardBusinessDocumentHeader().getDocumentIdentification().getInstanceIdentifier()).isEqualTo(BESTILLINGS_ID);
		assertThat(sbd.getStandardBusinessDocumentHeader().getDocumentIdentification().getType()).isEqualTo(AVTALTMELDING_FORRETNINGSMELDING);
		assertThat(sbd.getStandardBusinessDocumentHeader().getDocumentIdentification().getCreationDateAndTime()).isEqualTo(OffsetDateTime.parse(TEN_SECONDS_BEFORE));
		assertThat(sbd.getStandardBusinessDocumentHeader().getDocumentIdentification().getMultipleType()).isTrue();
		assertThat(sbd.getStandardBusinessDocumentHeader().getBusinessScope().getScope()).hasSize(1);
		assertThat(sbd.getStandardBusinessDocumentHeader().getBusinessScope().getScope())
				.anyMatch(scope -> SCOPE_CONVERSATION_ID.equals(scope.getType()))
				.flatExtracting(Scope::getInstanceIdentifier, Scope::getIdentifier)
				.contains(KONVERSASJON_ID, SCOPE_CONVERSATION_ID_IDENTIFIER);
		assertThat(sbd.getStandardBusinessDocumentHeader().getBusinessScope().getScope())
				.anyMatch(scope -> SCOPE_CONVERSATION_ID.equals(scope.getType()))
				.flatExtracting(Scope::getScopeInformation)
				.extracting(CorrelationInformation::getExpectedResponseDateTime)
				.contains(OffsetDateTime.parse(TWENTY_FOUR_HOURS_LATER));
		assertThat(sbd.getAny()).isInstanceOf(AvtaltMessage.class);
	}
}