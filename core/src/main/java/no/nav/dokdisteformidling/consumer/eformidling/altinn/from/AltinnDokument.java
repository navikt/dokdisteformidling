package no.nav.dokdisteformidling.consumer.eformidling.altinn.from;

import lombok.Builder;
import lombok.Value;
import no.altinn.schema.services.serviceengine.broker._2015._06.BrokerServiceManifest;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.sbdh.StandardBusinessDocument;

@Builder
@Value
public class AltinnDokument {
    public static final String MANIFEST_XML_FILENAME = "manifest.xml";
    public static final String STANDARDBUSINESSDOCUMENT_JSON_FILENAME = "sbd.json";

    private final BrokerServiceManifest manifest;
    private final StandardBusinessDocument sbd;
}
