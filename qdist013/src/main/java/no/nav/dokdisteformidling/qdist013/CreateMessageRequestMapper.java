package no.nav.dokdisteformidling.qdist013;

import static java.time.temporal.ChronoUnit.HOURS;
import static java.time.temporal.ChronoUnit.SECONDS;
import static no.nav.dokdisteformidling.qdist013.Qdist013Service.ARKIVMELDING;
import static no.nav.dokdisteformidling.qdist013.Qdist013Service.ARKIVMELDING_XML;

import no.nav.dokdisteformidling.consumer.integrasjonspunkt.CreateMessageRequest;
import no.nav.dokdisteformidling.consumer.integrasjonspunkt.StandardBusinessDocumentHeader;
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

	private static final String HEADER_VERSION = "1.0";
	private static final String TYPE_VERSION = "1.0";
	private static final String IDENTIFIER_AUTHORITY = "iso6523-actorid-upis";
	private static final String PREFIX_IDENTIFIER_VALUE = "0192:";
	private static final String NAV_ORG_NR = "889640782"; // hardkodet inntil videre fordi DPO-bruker er knyttet til denne (MMA-3834)
	private static final String TRYGDERETTEN_ORG_NR = "974761084";
	private static final String DOCUMENT_IDENTIFICATOR_STANDARD = "urn:no:difi:arkivmelding:xsd::arkivmelding";
	private static final String CONVERSATION_ID_SCOPE_IDENTIFIER = "urn:no:difi:profile:arkivmelding:administrasjon:ver1.0";
	private static final String CONVERSATION_ID = "ConversationId";
	private static final int SIKKERHETSNIVAA_4 = 4;

	public CreateMessageRequest map(String conversationId, HentForsendelseResponseTo hentForsendelseResponseTo) {
		return CreateMessageRequest.builder()
				.arkivmelding(CreateMessageRequest.Arkivmelding.builder()
						.hoveddokument(ARKIVMELDING_XML)
						.sikkerhetsnivaa(SIKKERHETSNIVAA_4)
						.build())
				.standardBusinessDocumentHeader(mapStandardBusinessDocumentHeader(conversationId, hentForsendelseResponseTo))
				.build();
	}

	private StandardBusinessDocumentHeader mapStandardBusinessDocumentHeader(String konversasjonsId, HentForsendelseResponseTo hentForsendelseResponseTo) {
		return StandardBusinessDocumentHeader.builder()
				.businessScope(StandardBusinessDocumentHeader.BusinessScope.builder()
						.scope(new HashSet<>(Collections.singletonList(StandardBusinessDocumentHeader.BusinessScope.Scope
								.builder()
								.identifier(CONVERSATION_ID_SCOPE_IDENTIFIER)
								.instanceIdentifier(konversasjonsId)
								.scopeInformation(new HashSet<>(Collections.singletonList(StandardBusinessDocumentHeader.BusinessScope.Scope.CorrelationInformation
										.builder()
										.expectedResponseDateTime(OffsetDateTime.now().plus(4, HOURS))
										.build())))
								.type(CONVERSATION_ID)
								.build()
						)))
						.build())
				.documentIdentification(StandardBusinessDocumentHeader.DocumentIdentification.builder()
						.creationDateAndTime(OffsetDateTime.now().minus(10, SECONDS))
						.instanceIdentifier(hentForsendelseResponseTo.getBestillingsId())
						.multipleType(Boolean.TRUE)
						.standard(DOCUMENT_IDENTIFICATOR_STANDARD)
						.type(ARKIVMELDING)
						.typeVersion(TYPE_VERSION)
						.build())
				.headerVersion(HEADER_VERSION)
				.receiver(new HashSet<>(Collections.singletonList(StandardBusinessDocumentHeader.Receiver.builder()
						.identifier(StandardBusinessDocumentHeader.Partner.PartnerIdentification.builder()
								.authority(IDENTIFIER_AUTHORITY)
								.value(PREFIX_IDENTIFIER_VALUE + TRYGDERETTEN_ORG_NR)
								.build())
						.build())))
				.sender(new HashSet<>(Collections.singletonList(StandardBusinessDocumentHeader.Sender.builder()
						.identifier(StandardBusinessDocumentHeader.Partner.PartnerIdentification.builder()
								.authority(IDENTIFIER_AUTHORITY)
								.value(PREFIX_IDENTIFIER_VALUE + NAV_ORG_NR)
								.build())
						.build())))
				.build();
	}
}