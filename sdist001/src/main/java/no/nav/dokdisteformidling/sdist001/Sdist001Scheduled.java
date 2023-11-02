package no.nav.dokdisteformidling.sdist001;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdisteformidling.consumer.leaderelection.LeaderElection;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static no.nav.dokdisteformidling.utils.MDCUtils.clearMDC;
import static no.nav.dokdisteformidling.utils.MDCUtils.generateNewCallId;

@Slf4j
@Component
public class Sdist001Scheduled {

	private final Sdist001Service sdist001Service;
	private final LeaderElection leaderElection;

	public Sdist001Scheduled(Sdist001Service sdist001Service,
							 LeaderElection leaderElection) {
		this.sdist001Service = sdist001Service;
		this.leaderElection = leaderElection;
	}

	@Scheduled(fixedDelayString = "${sdist001.intervall:600000}")
	public void triggerOppdatering() {
		if (leaderElection.isLeader()) {
			generateNewCallId();

			try {
				sdist001Service.oppdaterDokDistEformidlingStatus();
			} finally {
				clearMDC();
			}
		}
	}

}
