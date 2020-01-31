package no.nav.dokdisteformidling.consumer.eformidling.altinn.to;

import lombok.Builder;
import lombok.Value;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
@Builder
public class ReceiptTo {
	private final String lastChanged;
	private final Integer parentReceiptId;
	private final String receiptHistory;
	private final Integer receiptId;
	private final String receiptStatusCode;
	private final String receiptText;
	private final String receiptTypeName;
}
