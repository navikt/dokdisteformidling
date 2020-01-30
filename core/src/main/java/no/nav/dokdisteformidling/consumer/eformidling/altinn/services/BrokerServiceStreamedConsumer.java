package no.nav.dokdisteformidling.consumer.eformidling.altinn.services;

import lombok.extern.slf4j.Slf4j;
import no.altinn.brokerserviceexternalstreamed.IBrokerServiceExternalStreamed;
import no.altinn.brokerserviceexternalstreamed.ObjectFactory;
import no.altinn.brokerserviceexternalstreamed.ReceiptExternalStreamedBE;
import no.nav.dokdisteformidling.config.cxf.WssX509PropertyFactory;

import javax.activation.DataHandler;

@Slf4j
public class BrokerServiceStreamedConsumer {

    private IBrokerServiceExternalStreamed brokerServiceStreamed;
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
