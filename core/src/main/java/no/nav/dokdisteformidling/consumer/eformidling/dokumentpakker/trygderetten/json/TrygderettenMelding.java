
package no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.trygderetten.json;

import lombok.Data;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.sbdh.Scope;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.sbdh.ScopeType;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.sbdh.StandardBusinessDocumentHeader;

import java.util.Optional;

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

	public String getMessageChannelName() {
		return standardBusinessDocumentHeader.getBusinessScope().getScope().stream()
				.filter(MESSAGE_CHANNEL)
				.map(Scope::getIdentifier)
				.findAny().orElse("[ikke satt]");
	}

	public String getProcess() {
		return this.getScope(ScopeType.CONVERSATION_ID).map(Scope::getIdentifier).orElseThrow(() -> new IllegalStateException("SBD missing process"));
	}

	public Optional<Scope> getScope(ScopeType scopeType) {
		return Optional.ofNullable(this.standardBusinessDocumentHeader).flatMap((p) -> p.getScope(scopeType));
	}

}
