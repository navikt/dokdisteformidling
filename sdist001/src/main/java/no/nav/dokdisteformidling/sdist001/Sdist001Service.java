package no.nav.dokdisteformidling.sdist001;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokdisteformidling.consumer.eformidling.Eformidling;
import no.nav.dokdisteformidling.consumer.eformidling.altinn.from.DownloadResponse;
import no.nav.dokdisteformidling.consumer.juridisklogg.JuridiskLogg;
import no.nav.dokdisteformidling.consumer.juridisklogg.LagreJuridiskLoggMapper;
import no.nav.dokdisteformidling.consumer.juridisklogg.LoggMeldingRequest;
import no.nav.dokdisteformidling.consumer.rdist001.AdministrerForsendelse;
import no.nav.dokdisteformidling.consumer.rdist001.HentEformidlingforsendelserResponse.Forsendelse;
import no.nav.dokdisteformidling.consumer.rdist001.HentForsendelseResponse;
import no.nav.dokdisteformidling.consumer.rdist001.OppdaterForsendelseRequest;
import no.nav.dokdisteformidling.exception.functional.KunneIkkeSerialisereEformidlingstatusoppdateringTilJson;
import no.nav.dokdisteformidling.sdist001.domain.EformidlingStatusOppdatering;
import no.nav.dokdisteformidling.sdist001.domain.ForsendelseStatusEndringer;
import no.nav.dokdisteformidling.sdist001.domain.to.AltinnKvitteringStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Consumer;

import static java.time.LocalDateTime.now;
import static no.nav.dokdisteformidling.sdist001.domain.to.ForsendelseStatus.BEKREFTET;
import static no.nav.dokdisteformidling.sdist001.domain.to.ForsendelseStatus.EKSPEDERT;
import static no.nav.dokdisteformidling.sdist001.domain.to.ForsendelseStatus.FEIL;
import static no.nav.dokdisteformidling.sdist001.domain.to.ForsendelseStatus.OVERSENDT;

@Slf4j
@Service
public class Sdist001Service {

	private final AdministrerForsendelse administrerForsendelse;
	private final Eformidling eformidling;
	private final JuridiskLogg juridiskLogg;
	private final LagreJuridiskLoggMapper lagreJuridiskLoggMapper;
	private final ObjectMapper juridiskLoggObjectMapper;

	public Sdist001Service(AdministrerForsendelse administrerForsendelse,
						   JuridiskLogg juridiskLogg,
						   Eformidling eformidling,
						   LagreJuridiskLoggMapper lagreJuridiskLoggMapper) {
		this.administrerForsendelse = administrerForsendelse;
		this.juridiskLogg = juridiskLogg;
		this.lagreJuridiskLoggMapper = lagreJuridiskLoggMapper;
		this.eformidling = eformidling;
		this.juridiskLoggObjectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
	}

	public void oppdaterDokDistEformidlingStatus() {
		log.info("sdist001 starter regelmessig jobb for å oppdatere status for eFormidlingforsendelser");

		var endringer = new ForsendelseStatusEndringer();
		List<Forsendelse> forsendelserTo = administrerForsendelse.hentEformidlingForsendelser().getForsendelser();
		log.info("Hentet eformidlingforsendelser fra rdist001 {} ", forsendelserTo);

		eformidling.hent()
				.forEach(downloadResponse -> {
					log.info("Hentet trygderetten kvittering melding fra Altinn med konversasjonId={}, SendersReference={}, KvitteringStatus={}",
							downloadResponse.getConversationId(), downloadResponse.getSendersReference(), downloadResponse.getKvitteringStatus());
					forsendelserTo.stream()
							.filter(forsendelse -> validateForsendelse(forsendelse, downloadResponse))
							.forEach(behandleForsendelse(downloadResponse, endringer));
				});

		log.info("sdist001 har oppdatert status for eFormidlingforsendelser: {}", endringer);
	}

	private boolean validateForsendelse(Forsendelse forsendelse, DownloadResponse downloadResponse) {
		return downloadResponse.getConversationId().equals(forsendelse.getKonversasjonId()) &&
			   !EKSPEDERT.name().equals(forsendelse.getForsendelseStatus());
	}

	private Consumer<Forsendelse> behandleForsendelse(DownloadResponse downloadResponse, ForsendelseStatusEndringer endringer) {
		return forsendelse -> {
			String forsendelseId = forsendelse.getForsendelseId();
			log.info("sdist001 behandler forsendelse {}", forsendelse);

			try {
				kontrollerEformidlingStatus(downloadResponse.getKvitteringStatus().getStatus(), forsendelse, endringer);
				eformidling.bekreft(downloadResponse.getFileReference());
			} catch (Exception e) {
				log.error("sdist001 klarte ikke å behandle kvittering. forsendelseId={}", forsendelseId, e);
			}
		};
	}

	public void kontrollerEformidlingStatus(String kvitteringStatus, Forsendelse forsendelseTo, ForsendelseStatusEndringer endringer) {
		String forsendelseStatus = forsendelseTo.getForsendelseStatus();
		Long forsendelseId = Long.valueOf(forsendelseTo.getForsendelseId());
		String konversasjonId = forsendelseTo.getKonversasjonId();

		if (!OVERSENDT.name().equals(forsendelseStatus) && !BEKREFTET.name().equals(forsendelseStatus)) {
			log.warn("sdist001 forsendelseId={} med status={} ble feilaktig returnert av hentEformidlingForsendelser.", forsendelseId, forsendelseStatus);
			return;
		}

		oppdaterEformidlingStatus(kvitteringStatus, konversasjonId, forsendelseId, forsendelseStatus, endringer);
	}

	private void oppdaterEformidlingStatus(String kvitteringStatus,
										   String konversasjonId,
										   Long forsendelseId,
										   String forsendelseStatus,
										   ForsendelseStatusEndringer endringer) {

		AltinnKvitteringStatus altinnKvitteringStatus = AltinnKvitteringStatus.valueOf(kvitteringStatus);

		switch (altinnKvitteringStatus) {
			case SENDT:
				administrerForsendelse.oppdaterForsendelse(new OppdaterForsendelseRequest(forsendelseId, BEKREFTET.name(), null));
				endringer.getBekreftet().add(forsendelseId);
				break;
			case MOTTATT:
				log.info("sdist001 hentet eFormidlingforsendelser status fra Altinn med kvitteringStatus={}, konversasjonId={}", kvitteringStatus, konversasjonId);
				break;
			case LEVERT:
			case LEST:
				oppdaterTilEkspedert(altinnKvitteringStatus.name(), forsendelseId, konversasjonId);
				endringer.getEkspedert().add(forsendelseId);
				break;
			case FAIL:
				break;
			case LEVETID_UTLOPT:
				log.error("sdist001 avvik har oppstått for forsendelseId={}, konversasjonId={}. Forsendelsen settes til FEILET.", forsendelseId, konversasjonId);
				administrerForsendelse.oppdaterForsendelse(new OppdaterForsendelseRequest(forsendelseId, FEIL.name(), null));
				endringer.getFeilet().add(forsendelseId);
				break;
			default:
				log.error("sdist001 uventet status={} fra Altinn brokerservice for forsendelseId={} med forsendelseStatus={}.",
						altinnKvitteringStatus, forsendelseId, forsendelseStatus);
				break;
		}
	}

	private void oppdaterTilEkspedert(String trygderettenKvitteringStatus, Long forsendelseId, String konversasjonId) {
		HentForsendelseResponse hentForsendelseResponse = administrerForsendelse.hentForsendelse(forsendelseId);
		EformidlingStatusOppdatering eformidlingStatusOppdatering = new EformidlingStatusOppdatering(konversasjonId, trygderettenKvitteringStatus, now());

		try {
			byte[] meldingsInnhold = juridiskLoggObjectMapper.writeValueAsBytes(eformidlingStatusOppdatering);
			LoggMeldingRequest loggMeldingRequest = lagreJuridiskLoggMapper.map(hentForsendelseResponse, meldingsInnhold);
			juridiskLogg.lagreJuridiskLogg(loggMeldingRequest);
		} catch (JsonProcessingException e) {
			throw new KunneIkkeSerialisereEformidlingstatusoppdateringTilJson(
					"Kunne ikke serialisere eformidlingstatusoppdatering til JSON.", e);
		}

		administrerForsendelse.oppdaterForsendelse(new OppdaterForsendelseRequest(forsendelseId, EKSPEDERT.name(), null));
	}
}
