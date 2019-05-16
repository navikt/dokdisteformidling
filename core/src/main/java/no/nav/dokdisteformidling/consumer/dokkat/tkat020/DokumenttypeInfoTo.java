package no.nav.dokdisteformidling.consumer.dokkat.tkat020;

import lombok.Builder;
import lombok.Value;

/**
 * @author Sigurd Midttun, Visma Consulting AS
 */
@Value
@Builder
public class DokumenttypeInfoTo {

	private final String varselTypeId;
	private int sikkerhetsnivaa;
}
