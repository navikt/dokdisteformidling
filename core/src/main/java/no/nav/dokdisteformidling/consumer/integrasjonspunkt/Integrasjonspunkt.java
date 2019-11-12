package no.nav.dokdisteformidling.consumer.integrasjonspunkt;

import no.nav.dokdisteformidling.storage.DokdistDokument;

/**
 * @author Heidi Elisabeth Sando, Visma Consulting
 */
public interface Integrasjonspunkt {
	void opprettMelding(CreateMessageRequest createMessageRequest, String conversationId);

	void lastOppFil(DokdistDokument dokumenter, String title, String filename, String messageId);

	void sendMelding(String messageId);

	String getStatus(String conversationId);
}