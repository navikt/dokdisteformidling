package no.nav.dokdisteformidling.qdist011;

import static no.nav.dokdisteformidling.common.FunctionalUtils.deserializeS3JsonPayloadToDokdistDokument;
import static no.nav.dokdisteformidling.common.FunctionalUtils.getDokumenttypeIdHoveddokument;
import static no.nav.dokdisteformidling.common.FunctionalUtils.validateThatForsendelseStatusIsKlarForDist;

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
import org.apache.camel.Handler;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;


/**
 * @author Heidi Elisabeth Sando, Visma Consulting.
 */
@Component
public class Qdist011Service {

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
	public Qdist011Service(DokumentkatalogAdmin dokumentkatalogAdmin,
						   VarselInfo varselInfo,
						   AdministrerForsendelse administrerForsendelse,
						   Storage storage,
						   DigitalKontaktinformasjonV1 digitalKontaktinformasjonV1,
						   ProducerTemplate producer, BridgeMotSDPMapper bridgeMotSDPMapper,
						   DigitalKontaktInformasjonValidator digitalKontaktInformasjonValidator,
						   @Qualifier("SafJournalpostQueryServiceQdist011") SafJournalpostQueryService<JournalpostQdist011> safJournalpostQueryService) {
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
	public SendDigitalPost distribuerForsendelseTilDPIService(DistribuerForsendelseTilDpi distribuerForsendelseTilDpi) {

		HentForsendelseResponseTo hentForsendelseResponseTo = administrerForsendelse.hentForsendelse(distribuerForsendelseTilDpi
				.getForsendelseId());

		validateThatForsendelseStatusIsKlarForDist(hentForsendelseResponseTo.getForsendelseStatus());

		HentSikkerDigitalPostadresseResponseTo hentSikkerDigitalPostadresseResponseTo =
				digitalKontaktinformasjonV1.hentSikkerDigitalPostadresse(hentForsendelseResponseTo.getMottaker()
						.getMottakerId());

		DokumenttypeInfoTo dokumenttypeInfoTo = dokumentkatalogAdmin.getDokumenttypeInfo(getDokumenttypeIdHoveddokument(hentForsendelseResponseTo));


		VarselInfoTo varselInfoTo = getVarselInfoIfVarselTypeIdIsPresent(dokumenttypeInfoTo);

		digitalKontaktInformasjonValidator.validateKontaktinfo(hentSikkerDigitalPostadresseResponseTo, varselInfoTo);

		List<DokdistDokument> dokdistDokumentList = getDocumentsFromS3(hentForsendelseResponseTo);
		for (DokdistDokument dokdistDokument : dokdistDokumentList) {
			producer.sendBody(LastOppDokumentRoute.ROUTE, dokdistDokument);
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
