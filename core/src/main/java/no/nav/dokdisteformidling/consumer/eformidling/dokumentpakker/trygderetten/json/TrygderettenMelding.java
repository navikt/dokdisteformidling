
package no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.trygderetten.json;

import lombok.Data;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.sbdh.Scope;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.sbdh.StandardBusinessDocumentHeader;

import static no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.sbdh.ScopeType.CONVERSATION_ID;
import static no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.sbdh.ScopeType.MESSAGE_CHANNEL;

@Data
public class TrygderettenMelding {

	private StandardBusinessDocumentHeader standardBusinessDocumentHeader;
	private KvitteringStatus status;

	public String getConversationId() {
		return standardBusinessDocumentHeader.getBusinessScope().getScope().stream()
				.filter(CONVERSATION_ID)
				.findAny().orElseThrow(RuntimeException::new)
				.getInstanceIdentifier();
	}

	public String getMessageChannel() {
		return standardBusinessDocumentHeader.getBusinessScope().getScope().stream()
				.filter(MESSAGE_CHANNEL)
				.map(Scope::getInstanceIdentifier)
				.findAny().orElse("[ikke satt]");
	}

}
