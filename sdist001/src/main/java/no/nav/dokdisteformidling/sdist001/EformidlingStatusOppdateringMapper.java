package no.nav.dokdisteformidling.sdist001;

import no.nav.dokdisteformidling.sdist001.domain.EformidlingStatusOppdatering;
import org.joda.time.LocalDateTime;

public class EformidlingStatusOppdateringMapper {

	public EformidlingStatusOppdatering map(String konversasjonId, String trygderettenKvitteringStatus) {
		return EformidlingStatusOppdatering.builder()
				.konversasjonId(konversasjonId)
				.status(trygderettenKvitteringStatus)
				.statusTidspunkt(LocalDateTime.now())
				.build();
	}
}
