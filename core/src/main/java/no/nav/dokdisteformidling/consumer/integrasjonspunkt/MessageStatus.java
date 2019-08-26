package no.nav.dokdisteformidling.consumer.integrasjonspunkt;

import lombok.Value;
import no.nav.dokdisteformidling.exception.functional.IntegrasjonspunktRequestFunctionalException;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;

/**
 * @author Erik Bråten, Visma Consulting.
 */
@Value
public class MessageStatus {

	private Long convId;
	private String conversationId;
	private String description;
	private String lastUpdate;
	private String rawReceipt;
	private String status;
	private Long id;

	public OffsetDateTime getLastUpdateDate() {
		try {
			return OffsetDateTime.parse(lastUpdate);
		} catch (NullPointerException | DateTimeParseException e) {
			throw new IntegrasjonspunktRequestFunctionalException(String.format("Kunne ikke bestemme oppdateringstidspunkt " +
					"for en statusendring i svaret fra getStatus for conversationId=%s: %s", conversationId,
					e.getMessage()), e);
		}
	}

	public static String findLatestStatus(List<MessageStatus> statusList) {
		if (statusList == null || statusList.isEmpty()) {
			return null;
		}

		Comparator<MessageStatus> comparator = Comparator.comparing(MessageStatus::getLastUpdateDate);

		return statusList.stream().max(comparator).get().getStatus();
	}
}
