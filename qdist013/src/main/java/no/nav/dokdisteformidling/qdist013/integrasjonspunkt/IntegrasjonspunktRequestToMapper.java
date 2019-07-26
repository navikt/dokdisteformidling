package no.nav.dokdisteformidling.qdist013.integrasjonspunkt;

import no.nav.dokdisteformidling.consumer.integrasjonspunkt.IntegrasjonspunktRequestTo;
import no.nav.dokdisteformidling.consumer.rdist001.HentForsendelseResponseTo;
import org.joda.time.DateTime;
import org.unece.cefact.namespaces.standardbusinessdocumentheader.BusinessScope;
import org.unece.cefact.namespaces.standardbusinessdocumentheader.DocumentIdentification;
import org.unece.cefact.namespaces.standardbusinessdocumentheader.Partner;
import org.unece.cefact.namespaces.standardbusinessdocumentheader.PartnerIdentification;
import org.unece.cefact.namespaces.standardbusinessdocumentheader.Scope;
import org.unece.cefact.namespaces.standardbusinessdocumentheader.StandardBusinessDocumentHeader;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Heidi Elisabeth Sando, Visma Consulting AS
 */
public class IntegrasjonspunktRequestToMapper {

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

	public IntegrasjonspunktRequestTo map(String konversasjonsId, HentForsendelseResponseTo hentForsendelseResponseTo) {        //TODO Hvor kommer konversasjonsid fra?
		return IntegrasjonspunktRequestTo.builder()
				.any(IntegrasjonspunktRequestTo.Any.builder()
						.hoveddokument(null)        //TODO Hva skal fylles inn her?
						.sikkerhetsnivaa(0)        //TODO Hva skal fylles inn her?
						.build())
				.standardBusinessDocumentHeader(mapStandardBusinessDocumentHeader(konversasjonsId, hentForsendelseResponseTo))
				.build();
	}

	private StandardBusinessDocumentHeader mapStandardBusinessDocumentHeader(String konversasjonsId, HentForsendelseResponseTo hentForsendelseResponseTo) {
		return new StandardBusinessDocumentHeader(HEADER_VERSION, mapSender(), mapReceiver(hentForsendelseResponseTo),
				mapDocumentIdentification(hentForsendelseResponseTo), null, mapBusinessScope(konversasjonsId));
	}

	private List<Partner> mapSender() {
		List<Partner> senderList = new ArrayList<>();
		senderList.add(new Partner(new PartnerIdentification(SENDER_IDENTIFIER_VALUE, IDENTIFIER_AUTHORITY), null));
		return senderList;
	}

	private List<Partner> mapReceiver(HentForsendelseResponseTo hentForsendelseResponseTo) {

		List<Partner> receiverList = new ArrayList<>();
		receiverList.add(new Partner(new PartnerIdentification(PREFIX_IDENTIFIER_VALUE + hentForsendelseResponseTo.getMottaker()
				.getMottakerId(),
				IDENTIFIER_AUTHORITY), null));
		return receiverList;
	}

	private DocumentIdentification mapDocumentIdentification(HentForsendelseResponseTo hentForsendelseResponseTo) {
		return new DocumentIdentification(DOCUMENT_IDENTIFICATOR_STANDARD, TYPE_VERSION,
				hentForsendelseResponseTo.getBestillingsId(), ARKIVMELDING, true, DateTime.now());
	}

	private BusinessScope mapBusinessScope(String konversasjonsId) {
		Scope conversationId = new Scope(CONVERSATION_ID, konversasjonsId, SCOPE_IDENTIFIER, mapScopeInformation());
		Scope senderRef = new Scope(SENDER_REF, null, null, null);     //Todo: Hva skal stå her for identifier?
		Scope receiverRef = new Scope(RECEIVER_REF, null, null, null); //Todo: Hva skal stå her for identifier?

		List<Scope> scopes = new ArrayList<>();
		scopes.add(conversationId);
		scopes.add(senderRef);
		scopes.add(receiverRef);

		return new BusinessScope(scopes);
	}

	private List<Object> mapScopeInformation() {
		List<Object> scopeInformations = new ArrayList<>();
		DateTime expectedResponseDateTime = DateTime.now().plusHours(4);
		scopeInformations.add(expectedResponseDateTime);
		return scopeInformations;
	}
}