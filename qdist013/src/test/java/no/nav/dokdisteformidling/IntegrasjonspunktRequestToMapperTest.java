package no.nav.dokdisteformidling;

import static org.junit.jupiter.api.Assertions.assertEquals;

import no.nav.dokdisteformidling.consumer.integrasjonspunkt.CreateMessageRequest;
import no.nav.dokdisteformidling.consumer.rdist001.HentForsendelseResponseTo;
import no.nav.dokdisteformidling.qdist013.CreateMessageRequestMapper;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * @author Heidi Elisabeth Sando, Visma Consulting AS
 */
@Disabled("Fixme")
//FIXME
public class IntegrasjonspunktRequestToMapperTest {

	private final static String NAV_ORGNR = "";        //TODO Hva er Navs organisasjonsnummer?
	private final static String HEADER_VERSION = "1.0";
	private final static String TYPE_VERSION = "1.0";
	private final static String IDENTIFIER_AUTHORITY = "iso6523-actorid-upis";
	private final static String PREFIX_IDENTIFIER_VALUE = "0192:";
	private final static String SENDER_IDENTIFIER_VALUE = PREFIX_IDENTIFIER_VALUE + NAV_ORGNR;
	private final static String DOCUMENT_IDENTIFICATOR_STANDARD = "urn:no:difi:arkivmelding:xsd::arkivmelding";
	private final static String ARKIVMELDING = "arkivmelding";
	private final static String SCOPE_IDENTIFIER = "urn:no:difi:profile:arkivmelding:administrasjon:ver1.0";
	private final static String CONVERSATION_ID = "ConversationId";
	private final static String SENDER_REF = "senderRef";
	private final static String RECEIVER_REF = "receiverRef";
	private final static String KONVERSASJONS_ID = "konversasjonsId";
	private final static DateTime EXPECTED_RESPONSE_DATE_TIME = DateTime.now().plusHours(4);
	private final static String BESTILLINGS_ID = "bestillingsId";
	private final static String MOTTAKER_ID = "mottakerId";


	private final CreateMessageRequestMapper createMessageRequestMapper = new CreateMessageRequestMapper();

	@Test
	public void shouldMap() {
		CreateMessageRequest createMessageRequest = createMessageRequestMapper.map(KONVERSASJONS_ID, createHentForsendelseResponseTo());
//		assertScopes(createMessageRequest.getStandardBusinessDocumentHeader().getBusinessScope().getScopes());
//		assertDocumentIdentification(createMessageRequest.getStandardBusinessDocumentHeader()
//				.getDocumentIdentification());
		assertEquals(HEADER_VERSION, createMessageRequest.getStandardBusinessDocumentHeader().getHeaderVersion());
//		assertReceiver(createMessageRequest.getStandardBusinessDocumentHeader().getReceivers().get(0));
//		assertSender(createMessageRequest.getStandardBusinessDocumentHeader().getReceivers().get(0));
	}

	@Test
	public void shouldMapWithoutBestillingsId() {
		//Todo
		CreateMessageRequest createMessageRequest = createMessageRequestMapper.map(KONVERSASJONS_ID, HentForsendelseResponseTo
				.builder()
				.mottaker(HentForsendelseResponseTo.MottakerTo.builder()
						.mottakerId(MOTTAKER_ID)
						.build())
				.build());
	}

	@Test
	public void shouldMapWithoutKonversasjonsId() {
		//Todo
		CreateMessageRequest createMessageRequest = createMessageRequestMapper.map(null, HentForsendelseResponseTo
				.builder()
				.mottaker(HentForsendelseResponseTo.MottakerTo.builder()
						.mottakerId(MOTTAKER_ID)
						.build())
				.build());
	}

	@Test
	public void shouldFailwithoutMottakerId() {
		//TODO
		CreateMessageRequest createMessageRequest = createMessageRequestMapper.map(KONVERSASJONS_ID, HentForsendelseResponseTo
				.builder()
				.bestillingsId(BESTILLINGS_ID)
				.mottaker(HentForsendelseResponseTo.MottakerTo.builder()
						.mottakerId(null)
						.build())
				.build());
	}

//	private void assertSender(Partner sender) {
//		assertEquals(IDENTIFIER_AUTHORITY, sender.getIdentifier().getAuthority());
//		assertEquals(SENDER_IDENTIFIER_VALUE, sender.getIdentifier().getValue());
//	}
//
//	private void assertReceiver(Partner receiver) {
//		assertEquals(PREFIX_IDENTIFIER_VALUE + MOTTAKER_ID, receiver.getIdentifier().getValue());
//		assertEquals(IDENTIFIER_AUTHORITY, receiver.getIdentifier().getAuthority());
//	}
//
//	private void assertDocumentIdentification(DocumentIdentification documentIdentification) {
//		assertEquals(DateTime.now(), documentIdentification.getCreationDateAndTime());
//		assertEquals(BESTILLINGS_ID, documentIdentification.getInstanceIdentifier());
//		assertTrue(documentIdentification.getMultipleType());
//		assertEquals(DOCUMENT_IDENTIFICATOR_STANDARD, documentIdentification.getStandard());
//		assertEquals(ARKIVMELDING, documentIdentification.getType());
//		assertEquals(TYPE_VERSION, documentIdentification.getTypeVersion());
//	}
//
//	private void assertScopes(List<Scope> scopes) {
//
//		Scope conversationId = scopes.stream().filter(
//				scope -> CONVERSATION_ID.equals(scope.getType()))
//				.findAny()
//				.orElseGet(Scope::new);
//
//		assertEquals(conversationId.getIdentifier(), SCOPE_IDENTIFIER);
//		assertEquals(conversationId.getInstanceIdentifier(), KONVERSASJONS_ID);
//		assertEquals(conversationId.getScopeInformations()
//				.stream()
//				.findAny()
//				.orElseGet(Scope::new), EXPECTED_RESPONSE_DATE_TIME);        //Todo: Denne er kanskje feil?
//
//		Scope senderRef = scopes.stream()
//				.filter(scope -> SENDER_REF.equals(scope.getType()))
//				.findAny()
//				.orElseGet(Scope::new);
//		assertEquals(senderRef.getIdentifier(), null);        //Todo: Hva skal stå her?
//
//		Scope receiverRef = scopes.stream()
//				.filter(scope -> RECEIVER_REF.equals(scope.getType()))
//				.findAny()
//				.orElseGet(Scope::new);
//		assertEquals(receiverRef.getIdentifier(), null);        //Todo: Hva skal stå her?
//	}

	private HentForsendelseResponseTo createHentForsendelseResponseTo() {
		return HentForsendelseResponseTo.builder()
				.bestillingsId(BESTILLINGS_ID)
				.mottaker(HentForsendelseResponseTo.MottakerTo.builder()
						.mottakerId(MOTTAKER_ID)
						.build())
				.build();
	}
}
