package no.nav.dokdisteformidling.consumer.eformidling;

import lombok.Builder;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;

@Value
@Builder
public class NavDokumentpakke {

	String conversationId;
	String bestillingsId;

	NavDokument arkivmelding;
	@Builder.Default
	List<NavDokument> navDokumenter = new ArrayList<>();
}
