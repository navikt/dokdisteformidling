package no.nav.dokdisteformidling.qdist013;

import jakarta.jms.Queue;
import jakarta.xml.bind.JAXBContext;
import no.nav.dokdisteformidling.common.IdsProcessor;
import no.nav.dokdisteformidling.exception.functional.AbstractDokdisteformidlingFunctionalException;
import no.nav.meldinger.virksomhet.dokdistfordeling.qdist008.out.DistribuerTilKanal;
import org.apache.camel.ExchangePattern;
import org.apache.camel.ValidationException;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import static no.nav.dokdisteformidling.constants.RouteConstants.PROPERTY_BESTILLINGS_ID;
import static no.nav.dokdisteformidling.constants.RouteConstants.PROPERTY_FORSENDELSE_ID;
import static no.nav.dokdisteformidling.constants.RouteConstants.PROPERTY_JOURNALPOST_ID;
import static no.nav.dokdisteformidling.constants.RouteConstants.QDIST013_SERVICE_ID;
import static org.apache.camel.LoggingLevel.ERROR;
import static org.apache.camel.LoggingLevel.INFO;
import static org.apache.camel.LoggingLevel.WARN;

@Component
public class Qdist013Route extends RouteBuilder {

	private final Qdist013Service qdist013Service;
	private final Queue qdist013;
	private final Queue qdist013FunksjonellFeil;
	private final Queue qdist015;

	public Qdist013Route(Qdist013Service qdist013Service,
						 @Qualifier("qdist013") Queue qdist013,
						 @Qualifier("qdist013FunksjonellFeil") Queue qdist013FunksjonellFeil,
						 @Qualifier("qdist015") Queue qdist015) {
		this.qdist013Service = qdist013Service;
		this.qdist013 = qdist013;
		this.qdist013FunksjonellFeil = qdist013FunksjonellFeil;
		this.qdist015 = qdist015;
	}

	@Override
	public void configure() throws Exception {
		errorHandler(defaultErrorHandler()
				.maximumRedeliveries(0)
				.log(log)
				.logExhaustedMessageBody(false)
				.loggingLevel(ERROR));

		onException(AbstractDokdisteformidlingFunctionalException.class, ValidationException.class)
				.handled(true)
				.useOriginalMessage()
				.log(WARN, log, "${exception}; " + getIdsForLogging())
				.to("jms:" + qdist013FunksjonellFeil.getQueueName());

		from("jms:" + qdist013.getQueueName() +
				"?transacted=true")
				.routeId(QDIST013_SERVICE_ID)
				.setExchangePattern(ExchangePattern.InOnly)
				.process(new IdsProcessor())
				.log(INFO, log, "qdist013 har mottatt forsendelse med forsendelseId=${exchangeProperty." + PROPERTY_FORSENDELSE_ID + "}")
				.to("validator:no/nav/meldinger/virksomhet/dokdistfordeling/xsd/qdist008/out/distribuertilkanal.xsd")
				.setProperty("originalBody", body())
				.unmarshal(new JaxbDataFormat(JAXBContext.newInstance(DistribuerTilKanal.class)))
				.bean(qdist013Service)
				.log(INFO, log, "qdist013 har produsert og lagret avtalemelding for " + getIdsForLogging() + ". Bestiller distribusjon via qdist015.")
				.setBody(exchangeProperty("originalBody"))
				.to("jms:" + qdist015.getQueueName())
				.log(INFO, log, "qdist013 har lagt forsendelse på qdist015-kø for " + getIdsForLogging())
				.end();
	}

	public static String getIdsForLogging() {
		return "bestillingsId=${exchangeProperty." + PROPERTY_BESTILLINGS_ID + "}, " +
				"forsendelseId=${exchangeProperty." + PROPERTY_FORSENDELSE_ID + "} og " +
				"journalpostId=${exchangeProperty." + PROPERTY_JOURNALPOST_ID + "}";
	}
}
