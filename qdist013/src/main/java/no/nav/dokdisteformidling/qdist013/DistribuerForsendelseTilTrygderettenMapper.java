package no.nav.dokdisteformidling.qdist013;

import no.nav.meldinger.virksomhet.dokdistfordeling.qdist008.out.DistribuerTilKanal;
import org.apache.camel.Handler;
import org.springframework.stereotype.Component;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Component
public class DistribuerForsendelseTilTrygderettenMapper {

	@Handler
	public DistribuerForsendelseTilTrygderetten map(DistribuerTilKanal distribuerTilKanal) {
		return DistribuerForsendelseTilTrygderetten.builder()
				.forsendelseId(distribuerTilKanal.getForsendelseId())
				.build();
	}

}
