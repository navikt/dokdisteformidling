package no.nav.dokdisteformidling.consumer.norg2;

import lombok.Builder;
import lombok.Value;

/**
 * @author Sigurd Midttun, Visma Consulting.
 * <p>
 * This class contains only a subset of the fields in the actual response. Organisasjonsnummer is the only relevant field for dokdisteformidling
 */
@Value
@Builder
public class HentEnhetsInfoResponse {

	private final String organisasjonsnummer;

}
