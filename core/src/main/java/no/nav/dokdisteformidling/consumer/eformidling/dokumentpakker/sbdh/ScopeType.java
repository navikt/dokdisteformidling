package no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.sbdh;

import java.util.function.Predicate;

public enum ScopeType implements Predicate<Scope> {
	JOURNALPOST_ID("JournalpostId"),
	CONVERSATION_ID("ConversationId"),
	MESSAGE_CHANNEL("MessageChannel"),
	SENDER_REF("SenderRef"),
	RECEIVER_REF("ReceiverRef");

	private String fullname;

	ScopeType(String fullname) {
		this.fullname = fullname;
	}

	@Override
	public String toString() {
		return this.fullname;
	}

	@Override
	public boolean test(Scope scope) {
		return this.fullname.equals(scope.getType()) || this.name().equals(scope.getType());
	}
}
