package no.nav.dokdisteformidling.consumer.integrasjonspunkt;

import lombok.Value;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Comparator;

/**
 * @author Erik Bråten, Visma Consulting.
 */
@Value
public class MessageStatus {

	private Long convId;
	private String conversationId;
	private String description;
	private OffsetDateTime lastUpdate;
	private String rawReceipt;
	private String status;
	private Long id;

	public static String findLatestStatus(Collection<MessageStatus> statusList) {
		if (statusList == null || statusList.isEmpty()) {
			return null;
		}

		Comparator<MessageStatus> comparator = Comparator.comparing(MessageStatus::getLastUpdate);

		return statusList.stream().max(comparator).get().getStatus();
	}
}
