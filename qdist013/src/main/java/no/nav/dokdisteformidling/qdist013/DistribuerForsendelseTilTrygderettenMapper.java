package no.nav.dokdisteformidling.qdist013;

import no.nav.meldinger.virksomhet.dokdistfordeling.qdist008.out.DistribuerTilKanal;
import org.apache.camel.Handler;
import org.springframework.stereotype.Component;

@Component
public class DistribuerForsendelseTilTrygderettenMapper {

	@Handler
	public DistribuerForsendelseTilTrygderetten map(DistribuerTilKanal distribuerTilKanal) {
		return new DistribuerForsendelseTilTrygderetten(distribuerTilKanal.getForsendelseId());
	}

}
