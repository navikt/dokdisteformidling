package no.nav.dokdisteformidling.qdist013.integrasjonspunkt;

import static no.nav.dokdisteformidling.common.IntegrasjonspunktAssertionUtils.assertFieldOnIntegrasjonspunktBodyNotNullOrEmpty;
import static no.nav.dokdisteformidling.common.IntegrasjonspunktAssertionUtils.assertObjectOnIntegrasjonspunktBodyNotNull;

import no.nav.dokdisteformidling.consumer.integrasjonspunkt.IntegrasjonspunktRequestTo;
import org.springframework.stereotype.Component;
import org.unece.cefact.namespaces.standardbusinessdocumentheader.DocumentIdentification;
import org.unece.cefact.namespaces.standardbusinessdocumentheader.Partner;
import org.unece.cefact.namespaces.standardbusinessdocumentheader.Scope;
import org.unece.cefact.namespaces.standardbusinessdocumentheader.StandardBusinessDocumentHeader;

import java.util.List;

/**
 * @author Heidi Elisabeth Sando, Visma Consulting AS
 */
@Component
public class IntegrasjonspunktRequestValidator {

	private void validate(IntegrasjonspunktRequestTo integrasjonspunktRequestTo) {
		assertObjectOnIntegrasjonspunktBodyNotNull("sikkerhetsnivaa", integrasjonspunktRequestTo.getAny().getSikkerhetsnivaa());
		validateStandardDocumentHeader(integrasjonspunktRequestTo.getStandardBusinessDocumentHeader());
	}

	private void validateStandardDocumentHeader(StandardBusinessDocumentHeader standardBusinessDocumentHeader) {
		validateScope(standardBusinessDocumentHeader.getBusinessScope().getScopes());
		validateDocumentIdentification(standardBusinessDocumentHeader.getDocumentIdentification());
		assertFieldOnIntegrasjonspunktBodyNotNullOrEmpty("headerVersion", standardBusinessDocumentHeader.getHeaderVersion());
		validateSenderReceiver(standardBusinessDocumentHeader.getReceivers(), "receiver");
		validateSenderReceiver(standardBusinessDocumentHeader.getSenders(), "sender");
	}

	private void validateSenderReceiver(List<Partner> partners, String fieldName) {
		partners.forEach(
				partner -> {
					assertFieldOnIntegrasjonspunktBodyNotNullOrEmpty(fieldName + ".identifier.value", partner.getIdentifier()
							.getValue());
					assertFieldOnIntegrasjonspunktBodyNotNullOrEmpty(fieldName + "identifier.authorty", partner.getIdentifier()
							.getAuthority());
				}
		);
	}

	private void validateScope(List<Scope> scopes) {
		scopes.forEach(scope -> assertFieldOnIntegrasjonspunktBodyNotNullOrEmpty("scope.identifier", scope.getIdentifier()));
	}

	private void validateDocumentIdentification(DocumentIdentification documentIdentification) {
		assertFieldOnIntegrasjonspunktBodyNotNullOrEmpty("documentIdentification.standard", documentIdentification.getStandard());
		assertFieldOnIntegrasjonspunktBodyNotNullOrEmpty("documentIdentification.type", documentIdentification.getType());
		assertFieldOnIntegrasjonspunktBodyNotNullOrEmpty("documentIdentification.typeVersion", documentIdentification.getTypeVersion());
	}

}
