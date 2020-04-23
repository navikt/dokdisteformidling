package no.nav.dokdisteformidling.common;

import static no.nav.dokdisteformidling.constants.MdcConstants.CALL_ID;
import static no.nav.dokdisteformidling.constants.RouteConstants.PROPERTY_FORSENDELSE_ID;

import no.nav.dokdisteformidling.exception.functional.ForsendelseManglerForsendelseIdFunctionalException;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.xml.XPathBuilder;
import org.slf4j.MDC;

import java.util.UUID;

/**
 * @author Heidi Elisabeth Sando, Visma Consulting.
 */
public class IdsProcessor implements Processor {

	@Override
	public void process(Exchange exchange) {
		setOrGenerateCallIdToMdc(exchange);
		setForsendelseIdAsProperty(exchange);
	}

	private void setOrGenerateCallIdToMdc(Exchange exchange) {
		String callId = exchange.getIn().getHeader(CALL_ID, String.class);
		if (callId == null || callId.trim().isEmpty()) {
			callId = UUID.randomUUID().toString();
			exchange.getIn().setHeader(CALL_ID, callId);
		}
		MDC.put(CALL_ID, callId);
	}

	private void setForsendelseIdAsProperty(Exchange exchange) {
		String forsendelseId = XPathBuilder.xpath("//forsendelseId/text()").evaluate(exchange, String.class);
		if (forsendelseId == null || forsendelseId.trim().isEmpty()) {
			throw new ForsendelseManglerForsendelseIdFunctionalException(exchange.getFromRouteId() + " har mottatt forsendelse uten påkrevd felt forsendelseId");
		}
		exchange.setProperty(PROPERTY_FORSENDELSE_ID, forsendelseId);
	}
}
