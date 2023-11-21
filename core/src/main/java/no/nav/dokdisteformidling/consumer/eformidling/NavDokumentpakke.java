package no.nav.dokdisteformidling.consumer.eformidling;

import lombok.Builder;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Value
@Builder
public class NavDokumentpakke {

	String conversationId;
	String bestillingsId;
	UUID messageChannelInstanceIdentifier;

	NavDokument arkivmelding;
	@Builder.Default
	List<NavDokument> navDokumenter = new ArrayList<>();

}
