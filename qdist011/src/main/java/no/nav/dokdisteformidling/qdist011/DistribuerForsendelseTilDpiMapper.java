package no.nav.dokdisteformidling.qdist011;

import no.nav.dokdisteformidling.qdist011.domain.DistribuerForsendelseTilDpi;
import no.nav.meldinger.virksomhet.dokdistfordeling.qdist008.out.DistribuerTilKanal;
import org.apache.camel.Handler;
import org.springframework.stereotype.Component;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Component
public class DistribuerForsendelseTilDpiMapper {

	@Handler
	public DistribuerForsendelseTilDpi map(DistribuerTilKanal distribuerTilKanal) {
		return DistribuerForsendelseTilDpi.builder()
				.forsendelseId(distribuerTilKanal.getForsendelseId())
				.build();
	}

}
