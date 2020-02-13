
package no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.trygderetten.json;

import lombok.Data;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.sbdh.ScopeType;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.sbdh.StandardBusinessDocumentHeader;

@Data
public class TrygderettenMelding {
	private StandardBusinessDocumentHeader standardBusinessDocumentHeader;
	private KvitteringStatus status;

	public String getConversationId() {
		return standardBusinessDocumentHeader.getBusinessScope().getScope().stream()
				.filter(scope -> ScopeType.CONVERSATION_ID.toString().equals(scope.getType()))
				.findAny().orElseThrow(RuntimeException::new)
				.getInstanceIdentifier();
	}

}
