package no.nav.dokdisteformidling.common;

import no.nav.dokdisteformidling.exception.functional.ForsendelseManglerForsendelseIdFunctionalException;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.language.xpath.XPathBuilder;

import static no.nav.dokdisteformidling.constants.MdcConstants.MDC_CALL_ID;
import static no.nav.dokdisteformidling.constants.RouteConstants.PROPERTY_FORSENDELSE_ID;
import static no.nav.dokdisteformidling.utils.MDCUtils.getCallId;
import static no.nav.dokdisteformidling.utils.MDCUtils.setCallId;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class IdsProcessor implements Processor {

	@Override
	public void process(Exchange exchange) {
		setOrGenerateCallIdToMdc(exchange);
		setForsendelseIdAsProperty(exchange);
	}

	private void setOrGenerateCallIdToMdc(Exchange exchange) {
		final String callId = exchange.getIn().getHeader(MDC_CALL_ID, String.class);
		if (isBlank(callId)) {
			String newCallId = getCallId();
			exchange.getIn().setHeader(MDC_CALL_ID, newCallId);
		} else {
			setCallId(callId);
		}
	}

	private void setForsendelseIdAsProperty(Exchange exchange) {
		String forsendelseId = XPathBuilder.xpath("//forsendelseId/text()").evaluate(exchange, String.class);
		if (forsendelseId == null || forsendelseId.trim().isEmpty()) {
			throw new ForsendelseManglerForsendelseIdFunctionalException(exchange.getFromRouteId() + " har mottatt forsendelse uten påkrevd felt forsendelseId");
		}
		exchange.setProperty(PROPERTY_FORSENDELSE_ID, forsendelseId);
	}
}
