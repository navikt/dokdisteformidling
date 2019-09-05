package no.nav.dokdisteformidling.qdist013;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import no.nav.dokdisteformidling.consumer.integrasjonspunkt.CreateMessageRequest;
import no.nav.dokdisteformidling.consumer.rdist001.HentForsendelseResponseTo;
import org.junit.jupiter.api.Test;

import java.util.Set;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
class CreateMessageRequestMapperTest {

	private final static String CONVERSATION_ID = "convId";
	private final static String CONVERSATION_ID_TYPE = "ConversationId";
	private final static String ORGNR_FOR_ENHET = "889640782";
	private final static String HEADER_VERSION = "1.0";
	private final static String TYPE_VERSION = "1.0";
	private final static String IDENTIFIER_AUTHORITY = "iso6523-actorid-upis";
	private final static String PREFIX_IDENTIFIER_VALUE = "0192:";
	private final static String DOCUMENT_IDENTIFICATOR_STANDARD = "urn:no:difi:arkivmelding:xsd::arkivmelding";
	private final static String ARKIVMELDING = "arkivmelding";
	private final static String CONVERSATION_ID_IDENTIFIER = "urn:no:difi:profile:arkivmelding:administrasjon:ver1.0";
	private final static String BESTILLINGS_ID = "bestillingsId";
	private final static String MOTTAKER_ID = "mottakerId";
	private final static String HOVEDDOKUMENT_ARKIVMELDING = "arkivmelding.xml";
	private final static int SIKKERHETSNIVAA = 4;

	private final CreateMessageRequestMapper createMessageRequestMapper = new CreateMessageRequestMapper();

	@Test
	void shouldMap() {
		CreateMessageRequest createMessageRequest = createMessageRequestMapper.map(CONVERSATION_ID, ORGNR_FOR_ENHET, createHentforsendelseResponse());
		assertCreateMessageRequest(createMessageRequest);
	}

	private void assertCreateMessageRequest(CreateMessageRequest createMessageRequest) {
		assertNotNull(createMessageRequest);
		assertArkivmelding(createMessageRequest.getArkivmelding());
		assertStandardBusinessDocumentHeader(createMessageRequest.getStandardBusinessDocumentHeader());

	}

	private void assertArkivmelding(CreateMessageRequest.Arkivmelding arkivmelding) {
		assertNotNull(arkivmelding);
		assertEquals(HOVEDDOKUMENT_ARKIVMELDING, arkivmelding.getHoveddokument());
		assertEquals(SIKKERHETSNIVAA, arkivmelding.getSikkerhetsnivaa());
	}

	private void assertStandardBusinessDocumentHeader(CreateMessageRequest.StandardBusinessDocumentHeader standardBusinessDocumentHeader) {
		assertNotNull(standardBusinessDocumentHeader);
		assertScope(standardBusinessDocumentHeader.getBusinessScope());
		assertDocumentIdentification(standardBusinessDocumentHeader.getDocumentIdentification());
		assertEquals(HEADER_VERSION, standardBusinessDocumentHeader.getHeaderVersion());
		assertReceiver(standardBusinessDocumentHeader.getReceiver());
		assertSender(standardBusinessDocumentHeader.getSender());

	}

	private void assertReceiver(Set<CreateMessageRequest.StandardBusinessDocumentHeader.Receiver> receiver) {
		assertTrue(receiver != null && receiver.size() == 1);
		assertNotNull(receiver.iterator().next());
		assertEquals(IDENTIFIER_AUTHORITY, receiver.iterator().next().getIdentifier().getAuthority());
		assertEquals(PREFIX_IDENTIFIER_VALUE + MOTTAKER_ID, receiver.iterator().next().getIdentifier().getValue());
	}

	private void assertSender(Set<CreateMessageRequest.StandardBusinessDocumentHeader.Sender> senders) {
		assertTrue(senders != null && senders.size() == 1);
		assertNotNull(senders.iterator().next());
		assertEquals(IDENTIFIER_AUTHORITY, senders.iterator().next().getIdentifier().getAuthority());
		assertEquals(PREFIX_IDENTIFIER_VALUE + ORGNR_FOR_ENHET, senders.iterator().next().getIdentifier().getValue());
	}

	private void assertDocumentIdentification(CreateMessageRequest.StandardBusinessDocumentHeader.DocumentIdentification documentIdentification) {
		assertNotNull(documentIdentification);
		assertNotNull(documentIdentification.getCreationDateAndTime());
		assertEquals(BESTILLINGS_ID, documentIdentification.getInstanceIdentifier());
		assertEquals(Boolean.TRUE, documentIdentification.getMultipleType());
		assertEquals(DOCUMENT_IDENTIFICATOR_STANDARD, documentIdentification.getStandard());
		assertEquals(ARKIVMELDING, documentIdentification.getType());
		assertEquals(TYPE_VERSION, documentIdentification.getTypeVersion());
	}


	private void assertScope(CreateMessageRequest.StandardBusinessDocumentHeader.BusinessScope businessScope) {
		assertNotNull(businessScope);
		assertTrue(businessScope.getScope() != null && businessScope.getScope().size() == 1);

		CreateMessageRequest.StandardBusinessDocumentHeader.BusinessScope.Scope scopeConvId = businessScope.getScope()
				.iterator().next();
		assertEquals(CONVERSATION_ID, scopeConvId.getInstanceIdentifier());
		assertEquals(CONVERSATION_ID_TYPE, scopeConvId.getType());
		assertEquals(1, scopeConvId.getScopeInformation().size());
		assertNotNull(scopeConvId.getScopeInformation().iterator().next().getExpectedResponseDateTime());
		assertEquals(CONVERSATION_ID_IDENTIFIER, scopeConvId.getIdentifier());

	}

	private HentForsendelseResponseTo createHentforsendelseResponse() {
		return HentForsendelseResponseTo.builder()
				.bestillingsId(BESTILLINGS_ID)
				.mottaker(HentForsendelseResponseTo.MottakerTo.builder()
						.mottakerId(MOTTAKER_ID)
						.build())
				.build();
	}
}