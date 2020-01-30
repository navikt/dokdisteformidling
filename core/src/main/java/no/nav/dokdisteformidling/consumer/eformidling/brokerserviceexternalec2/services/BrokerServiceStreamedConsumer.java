package no.nav.dokdisteformidling.consumer.eformidling.brokerserviceexternalec2.services;

import lombok.extern.slf4j.Slf4j;
import no.altinn.brokerserviceexternalbasicstreamed.IBrokerServiceExternalBasicStreamed;
import no.altinn.brokerserviceexternalbasicstreamed.ObjectFactory;
import no.altinn.brokerserviceexternalbasicstreamed.ReceiptExternalStreamedBE;
import no.nav.dokdisteformidling.config.cxf.WssX509PropertyFactory;

import javax.activation.DataHandler;

@Slf4j
public class BrokerServiceStreamedConsumer {

    private IBrokerServiceExternalBasicStreamed brokerServiceStreamed;
    private WssX509PropertyFactory credential;
    private ObjectFactory objectFactory;

    public BrokerServiceStreamedConsumer(WssX509PropertyFactory credential) {
        this.credential = credential;
        this.objectFactory = new ObjectFactory();
    }

    protected ReceiptExternalStreamedBE uploadToAltinn(String fileReference, String file, DataHandler dataHandler) {

        return null;

    }
}
