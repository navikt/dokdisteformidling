package no.nav.dokdisteformidling.consumer.ereg;

import lombok.Builder;
import lombok.Value;

/**
 * This class contains only a subset of the fields in the actual response. Navn is the only relevant field for dokdisteformidling
 */
@Value
@Builder
public class EregHentNoekkelInfoResponse {

	Navn navn;

	@Value
	@Builder
	public static class Navn {
		String navnelinje1;
		String navnelinje2;
		String navnelinje3;
		String navnelinje4;
		String navnelinje5;
	}

}
