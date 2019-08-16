package no.nav.dokdisteformidling.consumer.integrasjonspunkt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Value;

import java.time.OffsetDateTime;
import java.util.Set;

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

	@Value
	@Builder
	public static class StandardBusinessDocumentHeader {
		private final String headerVersion;
		private final Set<Sender> sender;
		private final Set<Receiver> receiver;
		private final DocumentIdentification documentIdentification;
		private final BusinessScope businessScope;

		@AllArgsConstructor
		@EqualsAndHashCode
		@Getter
		public abstract static class Partner {
			protected final PartnerIdentification identifier;

			@Value
			@Builder
			public static class PartnerIdentification {
				private final String authority;
				private final String value;
			}
		}

		@EqualsAndHashCode(callSuper = true)
		public static class Sender extends Partner {
			@Builder
			public Sender(PartnerIdentification identifier) {
				super(identifier);
			}
		}

		@EqualsAndHashCode(callSuper = true)
		public static class Receiver extends Partner {
			@Builder
			public Receiver(PartnerIdentification identifier) {
				super(identifier);
			}
		}

		@Value
		@Builder
		public static class DocumentIdentification {
			private final String standard;
			private final String typeVersion;
			private final String instanceIdentifier;
			private final String type;
			private final Boolean multipleType;
			private final OffsetDateTime creationDateAndTime;
		}

		@Value
		@Builder
		public static class BusinessScope {
			private final Set<Scope> scope;

			@Value
			@Builder
			public static class Scope {
				private final String type;
				private final String instanceIdentifier;
				private final String identifier;
				private final Set<CorrelationInformation> scopeInformation;

				@Value
				@Builder
				public static class CorrelationInformation {
					private final OffsetDateTime expectedResponseDateTime;
				}

			}
		}
	}
}
