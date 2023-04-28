package no.nav.dokdisteformidling.common;

import no.nav.dokdisteformidling.consumer.rdist001.AdministrerForsendelse;
import no.nav.dokdisteformidling.consumer.rdist001.OppdaterForsendelseRequest;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;

import static no.nav.dokdisteformidling.constants.DomainConstants.FORSENDELSE_STATUS_OVERSENDT;
import static no.nav.dokdisteformidling.constants.RouteConstants.PROPERTY_CONVERSATION_ID;
import static no.nav.dokdisteformidling.constants.RouteConstants.PROPERTY_FORSENDELSE_ID;


@Component
public class DokdistAdministrerForsendelseUpdater {

	private final AdministrerForsendelse administrerForsendelse;

	public DokdistAdministrerForsendelseUpdater(AdministrerForsendelse administrerForsendelse) {
		this.administrerForsendelse = administrerForsendelse;
	}

	public void updateStatus(Exchange exchange) {
		final String forsendelseId = exchange.getProperty(PROPERTY_FORSENDELSE_ID, String.class);
		administrerForsendelse.oppdaterForsendelseStatusOgKonversasjonsId(OppdaterForsendelseRequest.builder()
				.forsendelseId(Long.valueOf(forsendelseId))
				.forsendelseStatus(FORSENDELSE_STATUS_OVERSENDT)
				.build());
	}

	public void updateStatusAndConversationId(Exchange exchange) {
		final String forsendelseId = exchange.getProperty(PROPERTY_FORSENDELSE_ID, String.class);
		final String conversationId = exchange.getProperty(PROPERTY_CONVERSATION_ID, String.class);
		administrerForsendelse.oppdaterForsendelseStatusOgKonversasjonsId(
				OppdaterForsendelseRequest.builder()
						.forsendelseId(Long.valueOf(forsendelseId))
						.konversasjonId(conversationId)
						.forsendelseStatus(FORSENDELSE_STATUS_OVERSENDT)
						.build());

	}

}
