package no.nav.dokdisteformidling.consumer.rdist001;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class HentEformidlingforsendelserResponse {

	List<Forsendelse> forsendelser;

	@Value
	@Builder
	public static class Forsendelse {
		String forsendelseId;
		String forsendelseStatus;
		String distribusjonKanal;
		String konversasjonId;
	}
}
