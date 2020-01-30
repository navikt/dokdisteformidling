package no.nav.dokdisteformidling.consumer.eformidling.altinn.to;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BrokerManifestTo {

    private String sender;
    private String senderReference;
    private String filename;
    private String serviceCode;
    private int editionCode;
}
