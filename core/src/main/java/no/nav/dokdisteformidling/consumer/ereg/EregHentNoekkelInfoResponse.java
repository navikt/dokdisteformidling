package no.nav.dokdisteformidling.consumer.ereg;

import lombok.Builder;
import lombok.Value;

/**
 * @author Sigurd Midttun, Visma Consulting.
 * <p>
 * This class contains only a subset of the fields in the actual response. Navn is the only relevant field for dokdisteformidling
 */
@Value
@Builder
public class EregHentNoekkelInfoResponse {

	private final Navn navn;

	@Value
	@Builder
	public static class Navn {
		private final String navnelinje1;
		private final String navnelinje2;
		private final String navnelinje3;
		private final String navnelinje4;
		private final String navnelinje5;
	}

}
