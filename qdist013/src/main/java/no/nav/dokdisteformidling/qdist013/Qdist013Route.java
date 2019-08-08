package no.nav.dokdisteformidling.qdist013;

import static no.nav.dokdisteformidling.constants.RouteConstants.PROPERTY_BESTILLINGS_ID;
import static no.nav.dokdisteformidling.constants.RouteConstants.PROPERTY_FORSENDELSE_ID;
import static no.nav.dokdisteformidling.constants.RouteConstants.QDIST013_SERVICE_ID;
import static org.apache.camel.LoggingLevel.ERROR;

import no.nav.dokdisteformidling.common.DokdistStatusUpdater;
import no.nav.dokdisteformidling.common.IdsProcessor;
import no.nav.dokdisteformidling.exception.functional.AbstractDokdisteformidlingFunctionalException;
import no.nav.meldinger.virksomhet.dokdistfordeling.qdist008.out.DistribuerTilKanal;
import org.apache.camel.ExchangePattern;
import org.apache.camel.LoggingLevel;
import org.apache.camel.ValidationException;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.apache.camel.spring.SpringRouteBuilder;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.jms.Queue;
import javax.xml.bind.JAXBContext;


/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Component
public class Qdist013Route extends SpringRouteBuilder {

	private final Qdist013Service qdist013Service;
	private final Queue qdist013;
	private final Queue qdist013FunksjonellFeil;
	private final Qdist013MetricsRoutePolicy qdist0013MetricsRoutePolicy;
	private final DistribuerForsendelseTilTrygderettenMapper distribuerForsendelseTilTrygderettenMapper;
	private final DokdistStatusUpdater dokdistStatusUpdater;


	@Inject
	public Qdist013Route(Qdist013Service qdist013Service,
						 Queue qdist013,
						 Queue qdist013FunksjonellFeil,
						 Qdist013MetricsRoutePolicy qdist0013MetricsRoutePolicy,
						 DistribuerForsendelseTilTrygderettenMapper distribuerForsendelseTilTrygderettenMapper,
						 DokdistStatusUpdater dokdistStatusUpdater) {
		this.qdist013Service = qdist013Service;
		this.qdist013 = qdist013;
		this.qdist013FunksjonellFeil = qdist013FunksjonellFeil;
		this.qdist0013MetricsRoutePolicy = qdist0013MetricsRoutePolicy;
		this.distribuerForsendelseTilTrygderettenMapper = distribuerForsendelseTilTrygderettenMapper;
		this.dokdistStatusUpdater = dokdistStatusUpdater;
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
				.log(LoggingLevel.WARN, log, "${exception}; " + getIdsForLogging())
				.to("jms:" + qdist013FunksjonellFeil.getQueueName());

		from("jms:" + qdist013.getQueueName() +
				"?transacted=true")
				.routeId(QDIST013_SERVICE_ID)
				.routePolicy(qdist0013MetricsRoutePolicy)
				.setExchangePattern(ExchangePattern.InOnly)
				.process(new IdsProcessor())
				.log(LoggingLevel.INFO, log, "qdist013 har mottatt forsendelse med " + getIdsForLogging())
				.to("validator:no/nav/meldinger/virksomhet/dokdistfordeling/xsd/qdist008/out/distribuertilkanal.xsd")
				.unmarshal(new JaxbDataFormat(JAXBContext.newInstance(DistribuerTilKanal.class)))
				.bean(distribuerForsendelseTilTrygderettenMapper)
				.bean(qdist013Service)
				.log(LoggingLevel.INFO, log, "qdist013 har videresendt forsendelse med " + getIdsForLogging() + " til DIFI for distribusjon via TRYGDERETTEN")
				.bean(dokdistStatusUpdater) //TODO Vi må også mest sannsynlig sette konversasjonsId gjennom denne tjenesten. Må avklares
				.log(LoggingLevel.INFO, log, "qdist013 har oppdatert forsendelseStatus i dokdist til OVERSENDT og avslutter behandling av forsendelse med " + getIdsForLogging())
				.end();
	}

	public static String getIdsForLogging() {
		return "bestillingsId=${exchangeProperty." + PROPERTY_BESTILLINGS_ID + "} og " +
				"forsendelseId=${exchangeProperty." + PROPERTY_FORSENDELSE_ID + "}";
	}
}
