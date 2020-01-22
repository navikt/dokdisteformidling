package no.nav.dokdisteformidling.consumer.integrasjonspunkt;

import lombok.Builder;
import lombok.Value;

/**
 * @author Heidi Elisabeth Sando, Visma Consulting AS
 */
@Value
@Builder
public class CreateMessageRequest {

	public final Arkivmelding arkivmelding;
	public final StandardBusinessDocumentHeader standardBusinessDocumentHeader;

	@Value
	@Builder
	public static class Arkivmelding {
		private final String hoveddokument;
		private final int sikkerhetsnivaa;
	}

}
