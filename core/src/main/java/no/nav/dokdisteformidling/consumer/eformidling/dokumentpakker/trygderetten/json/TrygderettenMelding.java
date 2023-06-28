
package no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.trygderetten.json;

import lombok.Data;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.sbdh.StandardBusinessDocumentHeader;

import static no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.sbdh.ScopeType.CONVERSATION_ID;

@Data
public class TrygderettenMelding {

	private StandardBusinessDocumentHeader standardBusinessDocumentHeader;
	private KvitteringStatus status;

	public String getConversationId() {
		return standardBusinessDocumentHeader.getBusinessScope().getScope().stream()
				.filter(scope -> CONVERSATION_ID.toString().equals(scope.getType()))
				.findAny().orElseThrow(RuntimeException::new)
				.getInstanceIdentifier();
	}

}
