package no.nav.dokdisteformidling.consumer.eformidling.brokerserviceexternalec2.mapper;

import no.altinn.brokerserviceexternaec2.Manifest;
import no.nav.dokdisteformidling.consumer.eformidling.brokerserviceexternalec2.to.BrokerManifestTo;

public class BrokerManifestMapper {

    public BrokerManifestTo mapBrokerManifest(Manifest manifest){

        return BrokerManifestTo.builder()
                .sender(manifest.getReportee())
                .senderReference(manifest.getSendersReference())
                .build();

    }
}
