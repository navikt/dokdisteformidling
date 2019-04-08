package no.nav.dokdisteformidling.consumer.regoppslag.to;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class HentMottakerOgAdresseResponseTo {

	private final AdresseTo adresse;
}
