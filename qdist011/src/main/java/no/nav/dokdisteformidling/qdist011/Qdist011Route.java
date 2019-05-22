package no.nav.dokdisteformidling.qdist011;

import static org.apache.camel.LoggingLevel.ERROR;

import com.google.common.base.Charsets;
import no.nav.dokdisteformidling.exception.functional.AbstractDokdisteformidlingFunctionalException;
import no.nav.meldinger.virksomhet.dokdistfordeling.qdist008.out.DistribuerTilKanal;
import org.apache.camel.ExchangePattern;
import org.apache.camel.LoggingLevel;
import org.apache.camel.ValidationException;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.apache.camel.spi.DataFormat;
import org.apache.camel.spring.SpringRouteBuilder;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.jms.Queue;
import javax.xml.bind.JAXBContext;


/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Component
public class Qdist011Route extends SpringRouteBuilder {

	public static final String QDIST011_SERVICE_ID = "qdist011";
	static final String PROPERTY_BESTILLINGS_ID = "bestillingsId";
	static final String PROPERTY_FORSENDELSE_ID = "forsendelseId";

	private final Qdist011Service qdist011Service;

	private final Queue qdist011;
	private final Queue qdist011FunksjonellFeil;
	private final Queue tdist005;
	private final Qdist011MetricsRoutePolicy qdist0011MetricsRoutePolicy;
	private final DistribuerForsendelseTilDpiMapper distribuerForsendelseTilDpiMapper;
	private final DokdistStatusUpdater dokdistStatusUpdater;

	private DataFormat digitalpostFormat = new JaxbDataFormat("no.nav.tjeneste.virksomhet.digitalpost.senddigitalpost.v1:no.difi.begrep.sdp.schema_v10");


	@Inject
	public Qdist011Route(Qdist011Service qdist011Service,
						 Queue qdist011,
						 Queue qdist011FunksjonellFeil,
						 Queue tdist005, Qdist011MetricsRoutePolicy qdist0011MetricsRoutePolicy,
						 DistribuerForsendelseTilDpiMapper distribuerForsendelseTilDpiMapper,
						 DokdistStatusUpdater dokdistStatusUpdater) {
		this.qdist011Service = qdist011Service;
		this.qdist011 = qdist011;
		this.qdist011FunksjonellFeil = qdist011FunksjonellFeil;
		this.tdist005 = tdist005;
		this.qdist0011MetricsRoutePolicy = qdist0011MetricsRoutePolicy;
		this.distribuerForsendelseTilDpiMapper = distribuerForsendelseTilDpiMapper;
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
				.to("jms:" + qdist011FunksjonellFeil.getQueueName());

		from("jms:" + qdist011.getQueueName() +
				"?transacted=true")
				.routeId(QDIST011_SERVICE_ID)
				.routePolicy(qdist0011MetricsRoutePolicy)
				.setExchangePattern(ExchangePattern.InOnly)
				.process(new IdsProcessor())
				.log(LoggingLevel.INFO, log, "qdist011 har mottatt forsendelse med " + getIdsForLogging())
				.to("validator:no/nav/meldinger/virksomhet/dokdistfordeling/xsd/qdist008/out/distribuertilkanal.xsd")
				.unmarshal(new JaxbDataFormat(JAXBContext.newInstance(DistribuerTilKanal.class)))
				.bean(distribuerForsendelseTilDpiMapper)
				.bean(qdist011Service)
				.marshal(digitalpostFormat).convertBodyTo(String.class, Charsets.UTF_8.toString())
				.to("jms:" + tdist005.getQueueName())
				.log(LoggingLevel.INFO, log, "qdist011 har lagt forsendelse med " + getIdsForLogging() + " på kø til tdist005for distribusjon via DPI")
				.bean(dokdistStatusUpdater)
				.log(LoggingLevel.INFO, log, "qdist011 har oppdatert forsendelseStatus i dokdist og avslutter behandling av forsendelse med " + getIdsForLogging());
	}

	public static String getIdsForLogging() {
		return "bestillingsId=${exchangeProperty." + PROPERTY_BESTILLINGS_ID + "} og " +
				"forsendelseId=${exchangeProperty." + PROPERTY_FORSENDELSE_ID + "}";
	}
}
