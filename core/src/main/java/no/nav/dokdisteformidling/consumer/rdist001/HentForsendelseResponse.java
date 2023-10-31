package no.nav.dokdisteformidling.consumer.rdist001;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class HentForsendelseResponse {

	String bestillingsId;
	String konversasjonId;
	String forsendelseStatus;
	String modus;
	String tema;
	String forsendelseTittel;
	Mottaker mottaker;
	ArkivInformasjon arkivInformasjon;
	Postadresse postadresse;
	List<Dokument> dokumenter;


	@Value
	@Builder
	public static class Mottaker {
		String mottakerId;
		String mottakerNavn;
		String mottakerType;
	}

	@Value
	@Builder
	public static class ArkivInformasjon {
		String arkivId;
	}

	@Value
	@Builder
	public static class Postadresse {
		String adresselinje1;
		String adresselinje2;
		String adresselinje3;
		String postnummer;
		String poststed;
		String landkode;
	}

	@Value
	@Builder
	public static class Dokument {
		String tilknyttetSom;
		String dokumentObjektReferanse;
		String arkivDokumentInfoId;
		String dokumenttypeId;
	}
}

