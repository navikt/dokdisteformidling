package no.nav.dokdisteformidling.consumer.eformidling.altinn.from;

import lombok.Builder;
import lombok.Value;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.trygderetten.json.TrygderettenMelding;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.trygderetten.xml.BrokerServiceManifest;

@Builder
@Value
public class AltinnDokument {
    public static final String MANIFEST_XML = "manifest.xml";
    public static final String SBD_JSON = "sbd.json";

    private final String fileReference;
    private final BrokerServiceManifest manifest;
    private final TrygderettenMelding trygderettenMelding;
}
