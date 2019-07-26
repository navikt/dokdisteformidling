package no.nav.dokdisteformidling.consumer.integrasjonspunkt;

import no.nav.dokdisteformidling.storage.DokdistDokument;

import java.util.List;

/**
 * @author Heidi Elisabeth Sando, Visma Consulting
 */
public interface Integrasjonspunkt {
	void opprettForsendelse(IntegrasjonspunktRequestTo integrasjonspunktRequestTo);

	void lastOppFiler(List<DokdistDokument> dokumenter, String arkivmeldingXMLString, String conversationId);

	void sendMelding(String conversationId);
}
