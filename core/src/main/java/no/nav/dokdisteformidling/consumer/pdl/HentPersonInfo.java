package no.nav.dokdisteformidling.consumer.pdl;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class HentPersonInfo {

	private String fulltnavn;
	private String ident;
}
