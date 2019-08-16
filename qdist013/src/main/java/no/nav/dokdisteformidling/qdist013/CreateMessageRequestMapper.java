package no.nav.dokdisteformidling.qdist013;

import static java.time.temporal.ChronoUnit.HOURS;
import static java.util.Arrays.asList;

import no.nav.dokdisteformidling.consumer.integrasjonspunkt.CreateMessageRequest;
import no.nav.dokdisteformidling.consumer.rdist001.HentForsendelseResponseTo;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashSet;

/**
 * @author Heidi Elisabeth Sando, Visma Consulting AS
 */
@Component
public class CreateMessageRequestMapper {

	private static final String NAV_ORGNR = "889640782";        //TODO Hva er Navs organisasjonsnummer? Kanskje 889640782?
	private static final String HEADER_VERSION = "1.0";
	private static final String TYPE_VERSION = "1.0";
	private static final String IDENTIFIER_AUTHORITY = "iso6523-actorid-upis";
	private static final String PREFIX_IDENTIFIER_VALUE = "0192:";
	private static final String DOCUMENT_IDENTIFICATOR_STANDARD = "urn:no:difi:arkivmelding:xsd::arkivmelding";
	private static final String ARKIVMELDING = "arkivmelding";
	private static final String CONVERSATION_ID_SCOPE_IDENTIFIER = "urn:no:difi:profile:arkivmelding:administrasjon:ver1.0";
	private static final String CONVERSATION_ID = "ConversationId";
	private static final String SENDER_REF = "SenderRef";
	private static final String RECEIVER_REF = "ReceiverRef";

	public CreateMessageRequest map(String conversationId, HentForsendelseResponseTo hentForsendelseResponseTo) {
		return CreateMessageRequest.builder()
				.arkivmelding(CreateMessageRequest.Arkivmelding.builder()
//						.hoveddokument()       TODO Må avklares
//						.sikkerhetsnivaa()     TODO Må avklares
						.build())
				.standardBusinessDocumentHeader(mapStandardBusinessDocumentHeader(conversationId, hentForsendelseResponseTo))
				.build();
	}

	private CreateMessageRequest.StandardBusinessDocumentHeader mapStandardBusinessDocumentHeader(String konversasjonsId, HentForsendelseResponseTo hentForsendelseResponseTo) {
		return CreateMessageRequest.StandardBusinessDocumentHeader.builder()
				.businessScope(CreateMessageRequest.StandardBusinessDocumentHeader.BusinessScope.builder()
						.scope(new HashSet<>(asList(CreateMessageRequest.StandardBusinessDocumentHeader.BusinessScope.Scope.builder()
										.identifier(CONVERSATION_ID_SCOPE_IDENTIFIER)
										.instanceIdentifier(konversasjonsId)
										.scopeInformation(new HashSet<>(Collections.singletonList(CreateMessageRequest.StandardBusinessDocumentHeader.BusinessScope.Scope.CorrelationInformation
												.builder()
												.expectedResponseDateTime(OffsetDateTime.now().plus(4, HOURS))
												.build())))
										.type(CONVERSATION_ID)
										.build(),
								CreateMessageRequest.StandardBusinessDocumentHeader.BusinessScope.Scope.builder()
//										.identifier() TODO Må avklares
										.type(SENDER_REF)
										.build(),
								CreateMessageRequest.StandardBusinessDocumentHeader.BusinessScope.Scope.builder()
//										.identifier() TODO Må avklares
										.type(RECEIVER_REF)
										.build()
						)))
						.build())
				.documentIdentification(CreateMessageRequest.StandardBusinessDocumentHeader.DocumentIdentification.builder()
						.instanceIdentifier(hentForsendelseResponseTo.getBestillingsId())
						.multipleType(Boolean.TRUE)
						.standard(DOCUMENT_IDENTIFICATOR_STANDARD)
						.type(ARKIVMELDING)
						.typeVersion(TYPE_VERSION)
						.build())
				.headerVersion(HEADER_VERSION)
				.receiver(new HashSet<>(Collections.singletonList(CreateMessageRequest.StandardBusinessDocumentHeader.Receiver.builder()
						.identifier(CreateMessageRequest.StandardBusinessDocumentHeader.Partner.PartnerIdentification.builder()
								.authority(IDENTIFIER_AUTHORITY)
								.value(PREFIX_IDENTIFIER_VALUE + hentForsendelseResponseTo.getMottaker().getMottakerId())
								.build())
						.build())))
				.sender(new HashSet<>(Collections.singletonList(CreateMessageRequest.StandardBusinessDocumentHeader.Sender.builder()
						.identifier(CreateMessageRequest.StandardBusinessDocumentHeader.Partner.PartnerIdentification.builder()
								.authority(IDENTIFIER_AUTHORITY)
								.value(PREFIX_IDENTIFIER_VALUE + NAV_ORGNR)
								.build())
						.build())))
				.build();
	}
}