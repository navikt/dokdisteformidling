package no.nav.dokdisteformidling.sdist001;

import no.nav.dokdisteformidling.sdist001.domain.EformidlingStatusOppdatering;
import org.joda.time.LocalDateTime;
import org.springframework.stereotype.Component;

/**
 * @author Erik Bråten, Visma Consulting.
 */
@Component
public class EformidlingStatusOppdateringMapper {

	public EformidlingStatusOppdatering map(String konversasjonId, String konversasjonStatus) {
		return EformidlingStatusOppdatering.builder()
				.konversasjonId(konversasjonId)
				.status(konversasjonStatus)
				.statusTidspunkt(LocalDateTime.now())
				.build();
	}
}
