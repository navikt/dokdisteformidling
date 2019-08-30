package no.nav.dokdisteformidling.sdist001.domain;

import lombok.Builder;
import lombok.Value;
import org.joda.time.LocalDateTime;

/**
 * @author Erik Bråten, Visma Consulting.
 */
@Value
@Builder
public class EformidlingStatusOppdatering {

	private String konversasjonId;
	private String status;
	private LocalDateTime statusTidspunkt;
}
