package no.nav.dokdisteformidling.common;

import static no.nav.dokdisteformidling.constants.DomainConstants.FORSENDELSE_STATUS_OVERSENDT;
import static no.nav.dokdisteformidling.constants.RouteConstants.PROPERTY_CONVERSATION_ID;
import static no.nav.dokdisteformidling.constants.RouteConstants.PROPERTY_FORSENDELSE_ID;

import no.nav.dokdisteformidling.consumer.rdist001.AdministrerForsendelse;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Component
public class DokdistAdministrerForsendelseUpdater {

	private final AdministrerForsendelse administrerForsendelse;

	public DokdistAdministrerForsendelseUpdater(AdministrerForsendelse administrerForsendelse) {
		this.administrerForsendelse = administrerForsendelse;
	}

	public void updateStatus(Exchange exchange) {
		final String forsendelseId = exchange.getProperty(PROPERTY_FORSENDELSE_ID, String.class);
		administrerForsendelse.oppdaterForsendelseStatus(forsendelseId, FORSENDELSE_STATUS_OVERSENDT);
	}

	public void updateStatusAndConversationId(Exchange exchange) {
		final String forsendelseId = exchange.getProperty(PROPERTY_FORSENDELSE_ID, String.class);
		final String conversationId = exchange.getProperty(PROPERTY_CONVERSATION_ID, String.class);
		administrerForsendelse.oppdaterForsendelseStatusOgKonversasjonsId(forsendelseId, FORSENDELSE_STATUS_OVERSENDT, conversationId);
	}

}
