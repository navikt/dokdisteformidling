package no.nav.dokdisteformidling.consumer.tps;

import lombok.Builder;
import lombok.Value;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Value
@Builder
public class TpsHentNavnResponse {

	private final String etternavn;
	private final String fornavn;
	private final String kortNavn;

}
