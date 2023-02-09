package no.nav.dokdisteformidling.consumer.rdist001;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class HentEformidlingforsendelserResponseTo {

	private final List<ForsendelseTo> forsendelser;

	@Data
	@Builder
	public static class ForsendelseTo {
		private final String forsendelseId;
		private final String forsendelseStatus;
		private final String distribusjonKanal;
		private final String konversasjonId;
	}
}
