package no.nav.dokdisteformidling.consumer.eformidling;

import no.nav.dokdisteformidling.consumer.eformidling.altinn.from.DownloadResponse;
import no.nav.dokdisteformidling.consumer.eformidling.altinn.to.UploadResponse;

import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public interface Eformidling {
    UploadResponse send(NavDokumentpakke navDokumentpakke, String arkivmelding);

    List<DownloadResponse> hent();

    void bekreft(String filreferanse);
}
