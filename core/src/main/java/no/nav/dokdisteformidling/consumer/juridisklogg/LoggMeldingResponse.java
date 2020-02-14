package no.nav.dokdisteformidling.consumer.juridisklogg;

import lombok.Builder;
import lombok.Value;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Value
@Builder
public class LoggMeldingResponse {
	private final String id;
}
