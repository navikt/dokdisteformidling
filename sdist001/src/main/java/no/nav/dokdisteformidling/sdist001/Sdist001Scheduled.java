package no.nav.dokdisteformidling.sdist001;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdisteformidling.constants.RetryConstants;
import no.nav.dokdisteformidling.consumer.rdist001.AdministrerForsendelse;
import no.nav.dokdisteformidling.consumer.rdist001.HentEformidlingforsendelserResponseTo;
import no.nav.dokdisteformidling.exception.functional.AbstractDokdisteformidlingFunctionalException;
import no.nav.dokdisteformidling.exception.technical.AbstractDokdisteformidlingTechnicalException;
import no.nav.dokdisteformidling.metrics.Monitor;
import no.nav.dokdisteformidling.sdist001.domain.ForsendelseStatusEndringer;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


/**
 * @author Erik Bråten, Visma Consulting.
 */
@Slf4j
@Component
public class Sdist001Scheduled {

	private final AdministrerForsendelse administrerForsendelse;
	private final Sdist001Service sdist001Service;

	public Sdist001Scheduled(AdministrerForsendelse administrerForsendelse,
							 Sdist001Service sdist001Service) {
		this.administrerForsendelse = administrerForsendelse;
		this.sdist001Service = sdist001Service;
	}

	@Scheduled(initialDelay = 300000, fixedDelayString = "${sdist001.intervall?:600000}")
	@Monitor(value = "dok_metric", extraTags = {"process", "oppdaterEformidlingStatus"}, histogram = true)
	public void oppdaterEformidlingStatus() {
		log.info("sdist001 oppdaterer status for eFormidlingforsendelser");

		ForsendelseStatusEndringer forsendelseStatusEndringer = new ForsendelseStatusEndringer();

		try {
			kontrollerOgOppdaterForsendelser(forsendelseStatusEndringer);
		} catch (Exception e) {
			log.error("sdist001 feilet under oppdatering av status for eFormidlingforsendelser: " + e.getMessage(), e);
			return;
		}

		log.info("sdist001 har oppdatert status for eFormidlingforsendelser: {}", forsendelseStatusEndringer.toString());
	}

	@Retryable(include = AbstractDokdisteformidlingTechnicalException.class, backoff = @Backoff(delay = RetryConstants.DELAY_LONG, multiplier = RetryConstants.MULTIPLIER_LONG))
	private void kontrollerOgOppdaterForsendelser(ForsendelseStatusEndringer forsendelseStatusEndringer) {
		HentEformidlingforsendelserResponseTo hentEformidlingforsendelserResponseTo = administrerForsendelse.hentEformidlingForsendelser();
		for (HentEformidlingforsendelserResponseTo.ForsendelseTo forsendelseTo : hentEformidlingforsendelserResponseTo.getForsendelser()) {
			try {
				sdist001Service.kontrollerOgOppdaterStatus(forsendelseTo, forsendelseStatusEndringer);
			} catch (AbstractDokdisteformidlingFunctionalException e) {
				log.warn(e.getMessage() + ". ForsendelseId=" + forsendelseTo.getForsendelseId(), e);
			}
		}
	}
}
