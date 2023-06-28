package no.nav.dokdisteformidling.consumer.eformidling.altinn.to;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ReceiptTo {
	String lastChanged;
	Integer parentReceiptId;
	String receiptHistory;
	Integer receiptId;
	String receiptStatusCode;
	String receiptText;
	String receiptTypeName;
}
