package no.nav.dokdisteformidling.consumer.juridisklogg;

import lombok.Builder;
import lombok.Value;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Value
@Builder
public class LoggMeldingRequest {
	private final String meldingsId;
	private final String avsender;
	private final String mottaker;
	private final String joarkRef;
	private final byte[] meldingsInnhold;
	private final Integer antallAarLagres;
}
