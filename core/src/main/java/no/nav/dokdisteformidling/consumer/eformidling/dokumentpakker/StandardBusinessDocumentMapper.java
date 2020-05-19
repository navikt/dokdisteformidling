package no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker;

import no.nav.dokdisteformidling.consumer.eformidling.EformidlingConstants;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.sbdh.BusinessScope;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.sbdh.CorrelationInformation;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.sbdh.DocumentIdentification;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.sbdh.PartnerIdentification;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.sbdh.Receiver;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.sbdh.Scope;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.sbdh.Sender;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.sbdh.StandardBusinessDocument;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.sbdh.StandardBusinessDocumentHeader;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

import static java.time.Duration.ofHours;
import static no.nav.dokdisteformidling.consumer.eformidling.Organisasjonsnummer.asIso6523;

/**
 * Mapper til konvoluttene til eformidling meldingen.
 *
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class StandardBusinessDocumentMapper {
	static final String HEADER_VERSION = "1.0";
	static final String TYPE_VERSION = "1.0";
	static final String IDENTIFIER_AUTHORITY = "iso6523-actorid-upis";
	static final String DOCUMENT_IDENTIFICATOR_STANDARD = "urn:no:difi:avtalt:xsd::avtalt";
	static final String SCOPE_CONVERSATION_ID = "ConversationId";
	static final String SCOPE_CONVERSATION_ID_IDENTIFIER = "urn:no:difi:profile:avtalt:avtalt:ver1.0";
	static final String AVTALTMELDING_FORRETNINGSMELDING = "avtalt";
	public static final String ARKIVMELDING_XML = "arkivmelding.xml";
	public static final int SIKKERHETSNIVAA = 4;
	public static final Duration EXPECTED_RESPONSE_WITHIN_HOURS = ofHours(24);

	private final Clock clock;

	@Inject
	public StandardBusinessDocumentMapper(Clock clock) {
		this.clock = clock;
	}

	StandardBusinessDocument mapAvtaltmeldingEnvelope(final String conversationId, final String bestillingsId) {
		StandardBusinessDocumentHeader standardBusinessDocumentHeader = new StandardBusinessDocumentHeader();
		standardBusinessDocumentHeader.setHeaderVersion(HEADER_VERSION);
		standardBusinessDocumentHeader.addSender(createSender());
		standardBusinessDocumentHeader.addReceiver(createReceiver());
		standardBusinessDocumentHeader.setDocumentIdentification(createDocumentIdentification(bestillingsId));
		BusinessScope businessScope = new BusinessScope();
		businessScope.addScope(createConversationIdScope(conversationId));
		standardBusinessDocumentHeader.setBusinessScope(businessScope);
		StandardBusinessDocument standardBusinessDocument = new StandardBusinessDocument();
		standardBusinessDocument.setStandardBusinessDocumentHeader(standardBusinessDocumentHeader);
		standardBusinessDocument.setAny(createAvtaltMessage());
		return standardBusinessDocument;
	}

	private DocumentIdentification createDocumentIdentification(final String instanceIdentifier) {
		DocumentIdentification documentIdentification = new DocumentIdentification();
		documentIdentification.setStandard(DOCUMENT_IDENTIFICATOR_STANDARD);
		documentIdentification.setTypeVersion(TYPE_VERSION);
		documentIdentification.setInstanceIdentifier(instanceIdentifier);
		documentIdentification.setType(AVTALTMELDING_FORRETNINGSMELDING);
		documentIdentification.setMultipleType(true);
		documentIdentification.setCreationDateAndTime(OffsetDateTime.now(clock).minus(10, ChronoUnit.SECONDS));
		return documentIdentification;
	}

	private Sender createSender() {
		final Sender sender = new Sender();
		final PartnerIdentification senderIdentification = new PartnerIdentification();
		senderIdentification.setAuthority(IDENTIFIER_AUTHORITY);
		senderIdentification.setValue(asIso6523(EformidlingConstants.NAV_ORGNUMMER));
		sender.setIdentifier(senderIdentification);
		return sender;
	}

	private Receiver createReceiver() {
		final Receiver receiver = new Receiver();
		final PartnerIdentification receiverIdentification = new PartnerIdentification();
		receiverIdentification.setAuthority(IDENTIFIER_AUTHORITY);
		receiverIdentification.setValue(asIso6523(EformidlingConstants.TRYGDERETTEN_ORGNUMMER));
		receiver.setIdentifier(receiverIdentification);
		return receiver;
	}

	private Scope createConversationIdScope(final String instanceIdentifier) {
		Scope conversationIdScope = new Scope();
		conversationIdScope.setType(SCOPE_CONVERSATION_ID);
		conversationIdScope.setInstanceIdentifier(instanceIdentifier);
		conversationIdScope.setIdentifier(SCOPE_CONVERSATION_ID_IDENTIFIER);
		final CorrelationInformation correlationInformation = new CorrelationInformation();
		correlationInformation.setExpectedResponseDateTime(OffsetDateTime.now(clock).plus(EXPECTED_RESPONSE_WITHIN_HOURS));
		conversationIdScope.addScopeInformation(correlationInformation);
		return conversationIdScope;
	}

	private AvtaltMessage createAvtaltMessage(){
		final AvtaltMessage forretningsmelding = new AvtaltMessage();
		forretningsmelding.setIdentifier(SCOPE_CONVERSATION_ID_IDENTIFIER);
		forretningsmelding.setContent(null);
		forretningsmelding.setSikkerhetsnivaa(SIKKERHETSNIVAA);
		forretningsmelding.setHoveddokument(ARKIVMELDING_XML);
		return forretningsmelding;
	}
}
