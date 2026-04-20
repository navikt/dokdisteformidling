package no.nav.dokdisteformidling.qdist013;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdisteformidling.consumer.rdist001.AdministrerForsendelseConsumer;
import no.nav.dokdisteformidling.consumer.rdist001.HentForsendelseResponse;
import no.nav.dokdisteformidling.consumer.rdist001.OppdaterForsendelseRequest;
import no.nav.dokdisteformidling.consumer.saf.SafJournalpostQueryService;
import no.nav.dokdisteformidling.exception.functional.InvalidForsendelseStatusFunctionalException;
import no.nav.dokdisteformidling.qdist013.avtaltmelding.Avtaltmelding;
import no.nav.dokdisteformidling.qdist013.avtaltmelding.AvtaltmeldingService;
import no.nav.dokdisteformidling.qdist013.saf.main.JournalpostQdist013;
import no.nav.meldinger.virksomhet.dokdistfordeling.qdist008.out.DistribuerTilKanal;
import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static no.nav.dokdisteformidling.constants.DomainConstants.FORSENDELSE_STATUS_KLAR_FOR_DIST;
import static no.nav.dokdisteformidling.constants.RouteConstants.PROPERTY_BESTILLINGS_ID;
import static no.nav.dokdisteformidling.constants.RouteConstants.PROPERTY_JOURNALPOST_ID;

@Slf4j
@Service
public class Qdist013Service {

	private static final String FORSENDELSE_METADATA_TYPE_DPO_AVTALEMELDING = "DPO_AVTALEMELDING";

	private final AdministrerForsendelseConsumer administrerForsendelse;
	private final SafJournalpostQueryService<JournalpostQdist013> safJournalpostQueryService;
	private final AvtaltmeldingService avtaltmeldingService;

	public Qdist013Service(AdministrerForsendelseConsumer administrerForsendelse,
						   @Qualifier("SafJournalpostQueryServiceQdist013") SafJournalpostQueryService<JournalpostQdist013> safJournalpostQueryService,
						   AvtaltmeldingService avtaltmeldingService) {
		this.administrerForsendelse = administrerForsendelse;
		this.safJournalpostQueryService = safJournalpostQueryService;
		this.avtaltmeldingService = avtaltmeldingService;
	}

	@Handler
	public void oppdaterForsendelseMetadata(DistribuerTilKanal distribuerTilKanal, Exchange exchange) {
		final Long forsendelseId = Long.valueOf(distribuerTilKanal.getForsendelseId());
		final HentForsendelseResponse hentForsendelseResponse = administrerForsendelse.hentForsendelse(forsendelseId);

		final String bestillingsId = hentForsendelseResponse.getBestillingsId();
		exchange.setProperty(PROPERTY_BESTILLINGS_ID, bestillingsId);

		final String journalpostId = hentForsendelseResponse.getArkivInformasjon().getArkivId();
		exchange.setProperty(PROPERTY_JOURNALPOST_ID, journalpostId);

		validateThatForsendelseStatusIsKlarForDist(hentForsendelseResponse.getForsendelseStatus());

		final JournalpostQdist013 journalpostQdist013 = safJournalpostQueryService.hentJournalpost(journalpostId);
		final Avtaltmelding avtaltmelding = avtaltmeldingService.map(journalpostQdist013, bestillingsId);

		log.info("Lagrer avtalemelding som forsendelseMetadata med type={}. forsendelseId={}, bestillingsId={}",
				FORSENDELSE_METADATA_TYPE_DPO_AVTALEMELDING, forsendelseId, bestillingsId);

		//forsendelseStatus og konversasjonId settes til null for at de ikke skal oppdateres
		administrerForsendelse.oppdaterForsendelse(new OppdaterForsendelseRequest(
				forsendelseId,
				null,
				null,
				avtaltmelding.asXmlString().getBytes(UTF_8),
				FORSENDELSE_METADATA_TYPE_DPO_AVTALEMELDING));
	}

	private static void validateThatForsendelseStatusIsKlarForDist(String forsendelseStatus) {
		if (!FORSENDELSE_STATUS_KLAR_FOR_DIST.equals(forsendelseStatus)) {
			throw new InvalidForsendelseStatusFunctionalException(
					format("ForsendelseStatus må være %s. Fant forsendelseStatus=%s", FORSENDELSE_STATUS_KLAR_FOR_DIST, forsendelseStatus)
			);
		}
	}
}
