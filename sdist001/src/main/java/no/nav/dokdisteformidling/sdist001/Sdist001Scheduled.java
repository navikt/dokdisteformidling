package no.nav.dokdisteformidling.sdist001;

import static no.nav.dokdisteformidling.constants.DomainConstants.FORSENDELSE_STATUS_BEKREFTET;
import static no.nav.dokdisteformidling.constants.DomainConstants.FORSENDELSE_STATUS_EKSPEDERT;
import static no.nav.dokdisteformidling.constants.DomainConstants.FORSENDELSE_STATUS_FEILET;
import static no.nav.dokdisteformidling.constants.DomainConstants.FORSENDELSE_STATUS_OVERSENDT;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdisteformidling.consumer.rdist001.AdministrerForsendelse;
import no.nav.dokdisteformidling.consumer.rdist001.HentEformidlingforsendelserResponseTo;
import no.nav.dokdisteformidling.exception.functional.AbstractDokdisteformidlingFunctionalException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


/**
 * @author Erik Bråten, Visma Consulting.
 */
@Slf4j
@Component
public class Sdist001Scheduled {

	private static final long POLL_RATE = 10000L; // TODO adjust to appropriate rate
	public static final String KONVERSASJON_STATUS_LEST = "LEST";
	public static final String KONVERSASJON_STATUS_LEVERT = "LEVERT";
	public static final String KONVERSASJON_STATUS_MOTTATT = "MOTTATT";
	public static final String KONVERSASJON_STATUS_OPPRETTET = "OPPRETTET";
	public static final String KONVERSASJON_STATUS_SENDT = "SENDT";
	public static final String KONVERSASJON_STATUS_TTL_EXPIRED = "TTL_EXPIRED";

	private final AdministrerForsendelse administrerForsendelse;

	public Sdist001Scheduled(AdministrerForsendelse administrerForsendelse) {
		this.administrerForsendelse = administrerForsendelse;
	}

	@Scheduled(fixedRate = POLL_RATE)
	public void oppdaterEformidlingStatus() {
		log.info("sdist001 oppdaterer status for eFormidlingforsendelser");

		HentEformidlingforsendelserResponseTo hentEformidlingforsendelserResponseTo = administrerForsendelse.hentEformidlingForsendelser();
		for (HentEformidlingforsendelserResponseTo.ForsendelseTo forsendelseTo : hentEformidlingforsendelserResponseTo.getForsendelser()) {
			try {
				kontrollerStatus(forsendelseTo);
			} catch (AbstractDokdisteformidlingFunctionalException e) {
				log.warn(e.getMessage() + ". ForsendelseId=" + forsendelseTo.getForsendelseId(), e);
			}
		}

		log.info("sdist001 har oppdatert status for eFormidlingforsendelser");
	}

	private void kontrollerStatus(HentEformidlingforsendelserResponseTo.ForsendelseTo forsendelseTo) {
		String forsendelseStatus = forsendelseTo.getForsendelseStatus();
		String forsendelseId = forsendelseTo.getForsendelseId().toString();

		if (!FORSENDELSE_STATUS_OVERSENDT.equals(forsendelseStatus) && !FORSENDELSE_STATUS_BEKREFTET.equals(forsendelseStatus)) {
			log.warn("ForsendelseId={} med status={} ble feilaktig returnert av hentEformidlingForsendelser.", forsendelseId, forsendelseStatus);
			return;
		}

		// TODO hent status fra integrasjonspunkt
		String konversasjonStatus = KONVERSASJON_STATUS_TTL_EXPIRED;
		konversasjonStatus = konversasjonStatus == null ? "" : konversasjonStatus;

		if (FORSENDELSE_STATUS_OVERSENDT.equals(forsendelseStatus)) {
			kontrollerStatusOversendt(konversasjonStatus, forsendelseId);
		} else {
			kontrollerStatusBekreftet(konversasjonStatus, forsendelseId);
		}
	}

	private void kontrollerStatusOversendt(String konversasjonStatus, String forsendelseId) {
		switch (konversasjonStatus) {
			case KONVERSASJON_STATUS_TTL_EXPIRED:
				log.error("Avvik har oppstått for forsendelseId={}. Forsendelsen settes til FEILET.", forsendelseId);
				administrerForsendelse.oppdaterForsendelseStatus(forsendelseId, FORSENDELSE_STATUS_FEILET);
				break;
			case KONVERSASJON_STATUS_OPPRETTET:
				// ingen endring
				break;
			case KONVERSASJON_STATUS_SENDT:
				administrerForsendelse.oppdaterForsendelseStatus(forsendelseId, FORSENDELSE_STATUS_BEKREFTET);
				break;
			default:
				log.warn("Uventet konversasjonstatus {} for forsendelseId={}.", konversasjonStatus, forsendelseId);
				break;
		}
	}

	private void kontrollerStatusBekreftet(String konversasjonStatus, String forsendelseId) {
		switch (konversasjonStatus) {
			case KONVERSASJON_STATUS_TTL_EXPIRED:
				log.error("Avvik har oppstått for forsendelseId={}. Forsendelsen settes til FEILET.", forsendelseId);
				administrerForsendelse.oppdaterForsendelseStatus(forsendelseId, FORSENDELSE_STATUS_FEILET);
				break;
			case KONVERSASJON_STATUS_SENDT:
				// ingen endring
				break;
			case KONVERSASJON_STATUS_MOTTATT:
			case KONVERSASJON_STATUS_LEVERT:
			case KONVERSASJON_STATUS_LEST:
				administrerForsendelse.oppdaterForsendelseStatus(forsendelseId, FORSENDELSE_STATUS_EKSPEDERT);
				// TODO skriv til juridisk logg
			default:
				log.warn("Uventet konversasjonstatus {} for forsendelseId={}.", konversasjonStatus, forsendelseId);
				break;
		}
	}
}
