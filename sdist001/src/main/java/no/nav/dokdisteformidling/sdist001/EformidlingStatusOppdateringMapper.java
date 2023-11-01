package no.nav.dokdisteformidling.sdist001;

import no.nav.dokdisteformidling.sdist001.domain.EformidlingStatusOppdatering;

import static java.time.LocalDateTime.now;

public class EformidlingStatusOppdateringMapper {

	public EformidlingStatusOppdatering map(String konversasjonId, String trygderettenKvitteringStatus) {

		return EformidlingStatusOppdatering.builder()
				.konversasjonId(konversasjonId)
				.status(trygderettenKvitteringStatus)
				.statusTidspunkt(now())
				.build();
	}
}
