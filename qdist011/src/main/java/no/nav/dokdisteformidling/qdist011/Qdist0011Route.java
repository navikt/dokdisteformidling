package no.nav.dokdisteformidling.qdist011;

import static no.nav.dokdisteformidling.constants.MdcConstants.CALL_ID;
import static org.apache.camel.LoggingLevel.ERROR;

import no.nav.dokdisteformidling.exception.functional.AbstractDokdisteformidlingFunctionalException;
import no.nav.meldinger.virksomhet.dokdistfordeling.qdist008.out.DistribuerTilKanal;
import org.apache.camel.ExchangePattern;
import org.apache.camel.LoggingLevel;
import org.apache.camel.ValidationException;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.apache.camel.spring.SpringRouteBuilder;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.jms.Queue;
import javax.xml.bind.JAXBContext;


/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Component
public class Qdist0011Route extends SpringRouteBuilder {

	public static final String QDIST011_SERVICE_ID = "qdist011";
	static final String PROPERTY_BESTILLINGS_ID = "bestillingsId";
	static final String PROPERTY_FORSENDELSE_ID = "forsendelseId";
	//fixme
//	private static final String SFTP_FILETYPE = ".zip";
//	private static final String SFTP_FILE_CONFIG = "binary=true&fileName=${exchangeProperty." + PROPERTY_BESTILLINGS_ID + "}" + SFTP_FILETYPE + "&";
//	private static final String SFTP_SECURITY_CONFIG = "privateKeyFile={{sftp.privateKeyFile}}&privateKeyPassphrase={{sftp.privateKeyPassphrase}}&preferredAuthentications=publickey";
//	private static final String SFTP_SERVER = "sftp://{{sftp.url}}:{{sftp.port}}/{{sftp.remoteFilePath}}?username={{sftp.username}}&***passord=gammelt_passord***;

	private final Qdist011Service qdist011Service;

	private final Queue qdist011;
	private final Queue qdist011FunksjonellFeil;
	private final Qdist011MetricsRoutePolicy qdist0011MetricsRoutePolicy;
	private final DistribuerForsendelseTilDpiMapper distribuerForsendelseTilDpiMapper;
	private final DokdistStatusUpdater dokdistStatusUpdater;


	@Inject
	public Qdist0011Route(Qdist011Service qdist011Service,
						  Queue qdist011,
						  Queue qdist011FunksjonellFeil,
						  Qdist011MetricsRoutePolicy qdist0011MetricsRoutePolicy,
						  DistribuerForsendelseTilDpiMapper distribuerForsendelseTilDpiMapper,
						  DokdistStatusUpdater dokdistStatusUpdater) {
		this.qdist011Service = qdist011Service;
		this.qdist011 = qdist011;
		this.qdist011FunksjonellFeil = qdist011FunksjonellFeil;
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
				.doTry()
				.setProperty(PROPERTY_BESTILLINGS_ID, simple("${in.header.callId}", String.class))
				.setProperty(PROPERTY_FORSENDELSE_ID, xpath("//forsendelseId/text()", String.class))
				.log(LoggingLevel.INFO, log, "qdist011 har mottatt forsendelse med " + getIdsForLogging())
				.process(exchange -> MDC.put(CALL_ID, (String) exchange.getProperty(PROPERTY_BESTILLINGS_ID)))
				.doCatch(Exception.class)
				.end()
				.to("validator:no/nav/meldinger/virksomhet/dokdistfordeling/xsd/qdist008/out/distribuertilkanal.xsd")
				.unmarshal(new JaxbDataFormat(JAXBContext.newInstance(DistribuerTilKanal.class)))
				.bean(distribuerForsendelseTilDpiMapper)
				.bean(qdist011Service)
//				.to(SFTP_SERVER fixme
				//todo: legg på kø til qdist005
				.log(LoggingLevel.INFO, log, "qdist011 har lagt forsendelse med " + getIdsForLogging() + " på NFS filshare for distribusjon via DPI")
				.bean(dokdistStatusUpdater)
				.log(LoggingLevel.INFO, log, "qdist011 har oppdatert forsendelseStatus i dokdist og avslutter behandling av forsendelse med " + getIdsForLogging());
	}

	public static String getIdsForLogging() {
		return "bestillingsId=${exchangeProperty." + PROPERTY_BESTILLINGS_ID + "} og " +
				"forsendelseId=${exchangeProperty." + PROPERTY_FORSENDELSE_ID + "}";
	}
}
