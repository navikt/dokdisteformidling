package no.nav.dokdisteformidling.consumer.eformidling;

import no.nav.dokdisteformidling.consumer.eformidling.altinn.from.DownloadResponse;

import java.util.List;

public interface Eformidling {

    void send(NavDokumentpakke navDokumentpakke, String arkivmelding);

    List<DownloadResponse> hent();

    void bekreft(String filreferanse);
}
