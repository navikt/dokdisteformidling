package no.nav.dokdisteformidling.qdist013;

import jakarta.jms.Queue;
import jakarta.xml.bind.JAXBContext;
import no.nav.dokdisteformidling.common.DokdistAdministrerForsendelseUpdater;
import no.nav.dokdisteformidling.common.IdsProcessor;
import no.nav.dokdisteformidling.exception.functional.AbstractDokdisteformidlingFunctionalException;
import no.nav.meldinger.virksomhet.dokdistfordeling.qdist008.out.DistribuerTilKanal;
import org.apache.camel.ExchangePattern;
import org.apache.camel.LoggingLevel;
import org.apache.camel.ValidationException;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.springframework.stereotype.Component;

import static no.nav.dokdisteformidling.constants.RouteConstants.PROPERTY_BESTILLINGS_ID;
import static no.nav.dokdisteformidling.constants.RouteConstants.PROPERTY_CONVERSATION_ID;
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
	private final DistribuerForsendelseTilTrygderettenMapper distribuerForsendelseTilTrygderettenMapper;
	private final DokdistAdministrerForsendelseUpdater dokdistAdministrerforsendelseUpdater;

	public Qdist013Route(Qdist013Service qdist013Service,
						 Queue qdist013,
						 Queue qdist013FunksjonellFeil,
						 DistribuerForsendelseTilTrygderettenMapper distribuerForsendelseTilTrygderettenMapper,
						 DokdistAdministrerForsendelseUpdater dokdistAdministrerforsendelseUpdater) {
		this.qdist013Service = qdist013Service;
		this.qdist013 = qdist013;
		this.qdist013FunksjonellFeil = qdist013FunksjonellFeil;
		this.distribuerForsendelseTilTrygderettenMapper = distribuerForsendelseTilTrygderettenMapper;
		this.dokdistAdministrerforsendelseUpdater = dokdistAdministrerforsendelseUpdater;
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
				.unmarshal(new JaxbDataFormat(JAXBContext.newInstance(DistribuerTilKanal.class)))
				.bean(distribuerForsendelseTilTrygderettenMapper)
				.bean(qdist013Service)
				.log(INFO, log, "qdist013 har sendt forsendelse med " + getIdsForLogging() + " til Trygderetten gjennom eFormidling. " +
								"Forsendelsen inneholder ${exchangeProperty.antallDok} dokumenter og avtaltmelding.")
				.bean(dokdistAdministrerforsendelseUpdater, "updateStatusAndConversationId")
				.log(INFO, log, "qdist013 har oppdatert dokdistDb med konversasjonsId=${exchangeProperty.conversationId} og forsendelseStatus=OVERSENDT og avslutter behandling av forsendelse med " + getIdsForLogging())
				.end();
	}

	public static String getIdsForLogging() {
		return "bestillingsId=${exchangeProperty." + PROPERTY_BESTILLINGS_ID + "}, " +
				"forsendelseId=${exchangeProperty." + PROPERTY_FORSENDELSE_ID + "} og " +
				"journalpostId=${exchangeProperty." + PROPERTY_JOURNALPOST_ID + "} og " +
				"conversationId=${exchangeProperty." + PROPERTY_CONVERSATION_ID + "}";
	}
}
