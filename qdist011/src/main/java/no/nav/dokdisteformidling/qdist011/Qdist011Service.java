package no.nav.dokdisteformidling.qdist011;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import no.nav.dokdisteformidling.consumer.dki.DigitalKontaktinformasjonV1;
import no.nav.dokdisteformidling.consumer.dki.HentSikkerDigitalPostadresseResponseTo;
import no.nav.dokdisteformidling.consumer.dokkat.tkat020.DokumentkatalogAdmin;
import no.nav.dokdisteformidling.consumer.dokkat.tkat020.DokumenttypeInfoTo;
import no.nav.dokdisteformidling.consumer.dokkat.tkat021.VarselInfo;
import no.nav.dokdisteformidling.consumer.dokkat.tkat021.VarselInfoTo;
import no.nav.dokdisteformidling.consumer.rdist001.AdministrerForsendelse;
import no.nav.dokdisteformidling.consumer.rdist001.HentForsendelseResponseTo;
import no.nav.dokdisteformidling.consumer.saf.SafJournalpostQueryService;
import no.nav.dokdisteformidling.qdist011.domain.DistribuerForsendelseTilDpi;
import no.nav.dokdisteformidling.qdist011.saf.JournalpostQdist011;
import no.nav.dokdisteformidling.storage.DokdistDokument;
import no.nav.dokdisteformidling.storage.Storage;
import no.nav.tjeneste.virksomhet.digitalpost.senddigitalpost.v1.SendDigitalPost;
import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

import static no.nav.dokdisteformidling.constants.RouteConstants.PROPERTY_BESTILLINGS_ID;
import static no.nav.dokdisteformidling.constants.RouteConstants.PROPERTY_CONVERSATION_ID;
import static no.nav.dokdisteformidling.metrics.MetricLabels.LABEL_PROCESS;
import static no.nav.dokdisteformidling.qdist011.Qdist011MetricsRoutePolicy.QDIST011_PROCESS_TIMER;
import static no.nav.dokdisteformidling.utils.FunctionalUtils.deserializeS3JsonPayloadToDokdistDokument;
import static no.nav.dokdisteformidling.utils.FunctionalUtils.getDokumenttypeIdHoveddokument;
import static no.nav.dokdisteformidling.utils.FunctionalUtils.validateThatForsendelseStatusIsKlarForDist;


/**
 * @author Heidi Elisabeth Sando, Visma Consulting.
 */
@Component
public class Qdist011Service {
	private final MeterRegistry meterRegistry;
	private final DokumentkatalogAdmin dokumentkatalogAdmin;
	private final VarselInfo varselInfo;
	private final AdministrerForsendelse administrerForsendelse;
	private final Storage storage;
	private final DigitalKontaktinformasjonV1 digitalKontaktinformasjonV1;
	private final ProducerTemplate producer;
	private final BridgeMotSDPMapper bridgeMotSDPMapper;
	private final DigitalKontaktInformasjonValidator digitalKontaktInformasjonValidator;
	private final SafJournalpostQueryService<JournalpostQdist011> safJournalpostQueryService;

	@Inject
	public Qdist011Service(MeterRegistry meterRegistry,
						   DokumentkatalogAdmin dokumentkatalogAdmin,
						   VarselInfo varselInfo,
						   AdministrerForsendelse administrerForsendelse,
						   Storage storage,
						   DigitalKontaktinformasjonV1 digitalKontaktinformasjonV1,
						   ProducerTemplate producer, BridgeMotSDPMapper bridgeMotSDPMapper,
						   DigitalKontaktInformasjonValidator digitalKontaktInformasjonValidator,
						   @Qualifier("SafJournalpostQueryServiceQdist011") SafJournalpostQueryService<JournalpostQdist011> safJournalpostQueryService) {
		this.meterRegistry = meterRegistry;
		this.dokumentkatalogAdmin = dokumentkatalogAdmin;
		this.varselInfo = varselInfo;
		this.administrerForsendelse = administrerForsendelse;
		this.storage = storage;
		this.digitalKontaktinformasjonV1 = digitalKontaktinformasjonV1;
		this.producer = producer;
		this.bridgeMotSDPMapper = bridgeMotSDPMapper;
		this.digitalKontaktInformasjonValidator = digitalKontaktInformasjonValidator;
		this.safJournalpostQueryService = safJournalpostQueryService;
	}

	@Handler
	public SendDigitalPost distribuerForsendelseTilDPIService(DistribuerForsendelseTilDpi distribuerForsendelseTilDpi, Exchange exchange) {

		HentForsendelseResponseTo hentForsendelseResponseTo = administrerForsendelse.hentForsendelse(distribuerForsendelseTilDpi
				.getForsendelseId());
		exchange.setProperty(PROPERTY_BESTILLINGS_ID, hentForsendelseResponseTo.getBestillingsId());
		exchange.setProperty(PROPERTY_CONVERSATION_ID, hentForsendelseResponseTo.getBestillingsId());

		validateThatForsendelseStatusIsKlarForDist(hentForsendelseResponseTo.getForsendelseStatus());

		HentSikkerDigitalPostadresseResponseTo hentSikkerDigitalPostadresseResponseTo =
				digitalKontaktinformasjonV1.hentSikkerDigitalPostadresse(hentForsendelseResponseTo.getMottaker()
						.getMottakerId());

		DokumenttypeInfoTo dokumenttypeInfoTo = dokumentkatalogAdmin.getDokumenttypeInfo(getDokumenttypeIdHoveddokument(hentForsendelseResponseTo));


		VarselInfoTo varselInfoTo = getVarselInfoIfVarselTypeIdIsPresent(dokumenttypeInfoTo);

		digitalKontaktInformasjonValidator.validateKontaktinfo(hentSikkerDigitalPostadresseResponseTo, varselInfoTo);

		List<DokdistDokument> dokdistDokumentList = getDocumentsFromS3(hentForsendelseResponseTo);
		for (DokdistDokument dokdistDokument : dokdistDokumentList) {
			Timer.Sample lastOppDokumentSample = Timer.start(meterRegistry);
			try {
				producer.sendBody(LastOppDokumentRoute.ROUTE, dokdistDokument);
			} finally {
				lastOppDokumentSample.stop(Timer.builder(QDIST011_PROCESS_TIMER)
						.description("Sample for opplasting til NFS share gjennom SFTP.")
						.tags(LABEL_PROCESS, "lastoppdokument")
						.publishPercentileHistogram(true)
						.register(meterRegistry));
			}
		}

		JournalpostQdist011 journalpostQdist011 = safJournalpostQueryService.hentJournalpost(hentForsendelseResponseTo.getArkivInformasjon()
				.getArkivId());

		return bridgeMotSDPMapper.map(hentForsendelseResponseTo, hentSikkerDigitalPostadresseResponseTo, dokumenttypeInfoTo, varselInfoTo, journalpostQdist011);
	}

	private VarselInfoTo getVarselInfoIfVarselTypeIdIsPresent(DokumenttypeInfoTo dokumenttypeInfoTo) {
		if (dokumenttypeInfoTo == null) {
			return null;
		} else {
			return varselInfo.getVarselInfo(dokumenttypeInfoTo.getVarselTypeId());
		}
	}

	private List<DokdistDokument> getDocumentsFromS3(HentForsendelseResponseTo hentForsendelseResponseTo) {
		return hentForsendelseResponseTo.getDokumenter().stream()
				.map(dokumentTo -> {
					String jsonPayload = storage.get(dokumentTo.getDokumentObjektReferanse());
					return deserializeS3JsonPayloadToDokdistDokument(jsonPayload, dokumentTo.getDokumentObjektReferanse());
				})
				.collect(Collectors.toList());
	}

}
