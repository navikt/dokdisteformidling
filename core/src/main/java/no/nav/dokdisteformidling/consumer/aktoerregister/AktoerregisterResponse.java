package no.nav.dokdisteformidling.consumer.aktoerregister;

import lombok.Builder;
import lombok.Value;

import java.util.Map;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Value
@Builder
public class AktoerregisterResponse {

	private final Map<String, IdentInfoForAktoer> identer;
}
