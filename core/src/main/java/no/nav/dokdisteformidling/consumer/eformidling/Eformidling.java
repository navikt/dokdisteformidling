package no.nav.dokdisteformidling.consumer.eformidling;

import no.nav.dokdisteformidling.consumer.eformidling.altinn.to.DownloadResponse;
import no.nav.dokdisteformidling.consumer.eformidling.altinn.to.UploadResponse;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public interface Eformidling {
	UploadResponse send(NavDokumentpakke navDokumentpakke);

	DownloadResponse hent();
}
