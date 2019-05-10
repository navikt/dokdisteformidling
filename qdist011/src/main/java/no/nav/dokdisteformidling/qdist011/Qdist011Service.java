package no.nav.dokdisteformidling.qdist011;

import no.nav.dokdisteformidling.consumer.dki.DigitalKontaktinformasjonV1;
import no.nav.dokdisteformidling.consumer.dki.HentSikkerDigitalPostadresseResponseTo;
import no.nav.dokdisteformidling.consumer.dokkat.tkat020.DokumentkatalogAdmin;
import no.nav.dokdisteformidling.consumer.dokkat.tkat021.VarselInfo;
import no.nav.dokdisteformidling.consumer.rdist001.AdministrerForsendelse;
import no.nav.dokdisteformidling.consumer.rdist001.HentForsendelseResponseTo;
import no.nav.dokdisteformidling.qdist011.domain.DistribuerForsendelseTilDpi;
import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Component
public class Qdist011Service {

	private final AdministrerForsendelse administrerForsendelse;
	private final DigitalKontaktinformasjonV1 digitalKontaktinformasjonV1;
	private final DokumentkatalogAdmin dokumentkatalogAdmin;
	private final VarselInfo varselInfo;

	@Inject
	public Qdist011Service(AdministrerForsendelse administrerForsendelse,
						   DigitalKontaktinformasjonV1 digitalKontaktinformasjonV1,
						   DokumentkatalogAdmin dokumentkatalogAdmin,
						   VarselInfo varselInfo) {
		this.administrerForsendelse = administrerForsendelse;
		this.digitalKontaktinformasjonV1 = digitalKontaktinformasjonV1;
		this.dokumentkatalogAdmin = dokumentkatalogAdmin;
		this.varselInfo = varselInfo;
	}

	@Handler
	public void distribuerForsendelseTilDpiService(DistribuerForsendelseTilDpi distribuerForsendelseTilDpi, Exchange exchange) {
		HentForsendelseResponseTo hentForsendelseResponseTo = administrerForsendelse.hentForsendelse(distribuerForsendelseTilDpi
				.getForsendelseId());
		// todo validere forsendelse

		HentSikkerDigitalPostadresseResponseTo hentSikkerDigitalPostadresseResponseTo =
				digitalKontaktinformasjonV1.hentSikkerDigitalPostadresse(hentForsendelseResponseTo.getMottaker().getMottakerId());

		// todo resterende logikk
	}
}
