package no.nav.dokdisteformidling.consumer.dokkat.tkat021;

import lombok.Builder;
import lombok.Value;

import java.util.Map;
import java.util.Set;

/**
 * @author Olav Røstvold Thorsen, Visma Consulting.
 */
@Value
@Builder
public class VarselInfoTo {

	private final String varselTypeId;
	private final boolean stoppRepeterendeVarsel;
	private final Map<String, String> varslingsTekst;
	private final String antallDagerListe;
	private final Set<String> preferertKanal;
}
