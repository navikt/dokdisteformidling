package no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.sbdh;

public enum ScopeType {
	JOURNALPOST_ID("JournalpostId"),
	CONVERSATION_ID("ConversationId"),
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
}
