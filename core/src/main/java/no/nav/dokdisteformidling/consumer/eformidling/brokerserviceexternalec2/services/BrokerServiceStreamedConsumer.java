package no.nav.dokdisteformidling.consumer.eformidling.brokerserviceexternalec2.services;

import lombok.extern.slf4j.Slf4j;
import no.altinn.brokerserviceexternalec2streamed.IBrokerServiceExternalEC2Streamed;
import no.altinn.brokerserviceexternalec2streamed.ObjectFactory;
import no.altinn.brokerserviceexternalec2streamed.ReceiptExternalStreamedBE;
import no.nav.dokdisteformidling.certificate.SecurityCredential;
import no.nav.dokdisteformidling.consumer.eformidling.brokerserviceexternalec2.AltinnBrokerServiceConsumerFactory;

import javax.activation.DataHandler;

@Slf4j
public class BrokerServiceStreamedConsumer {

    private AltinnBrokerServiceConsumerFactory consumerFactory;
    private IBrokerServiceExternalEC2Streamed brokerServiceStreamed;
    private SecurityCredential credential;
    private ObjectFactory objectFactory;

    public BrokerServiceStreamedConsumer(SecurityCredential credential) {
        this.credential = credential;
        this.objectFactory= new ObjectFactory();
        this.brokerServiceStreamed= consumerFactory.getBrokerServiceExternalEC2Streamed(credential);
    }

    protected ReceiptExternalStreamedBE uploadToAltinn(String fileReference, String file, DataHandler dataHandler) {

      return null;

    }
}
