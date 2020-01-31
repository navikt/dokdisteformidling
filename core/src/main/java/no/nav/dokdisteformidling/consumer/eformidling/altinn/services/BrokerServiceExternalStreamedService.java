package no.nav.dokdisteformidling.consumer.eformidling.altinn.services;

import lombok.extern.slf4j.Slf4j;
import no.altinn.brokerserviceexternalstreamed.IBrokerServiceExternalStreamed;
import no.altinn.brokerserviceexternalstreamed.IBrokerServiceExternalStreamedUploadFileStreamedAltinnFaultFaultFaultMessage;
import no.altinn.brokerserviceexternalstreamed.ObjectFactory;
import no.altinn.brokerserviceexternalstreamed.ReceiptExternalStreamedBE;
import no.altinn.brokerserviceexternalstreamed.StreamedPayloadExternalBE;
import org.apache.cxf.headers.Header;
import org.apache.cxf.jaxb.JAXBDataBinding;
import org.springframework.stereotype.Component;

import javax.activation.DataHandler;
import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class BrokerServiceExternalStreamedService {

    private static final String REPORTEE = "NAV";

    private IBrokerServiceExternalStreamed brokerServiceExternalStreamed;
    private ObjectFactory objectFactory;

    @Inject
    public BrokerServiceExternalStreamedService(final IBrokerServiceExternalStreamed brokerServiceExternalStreamed) {
        this.brokerServiceExternalStreamed = brokerServiceExternalStreamed;
        this.objectFactory = new ObjectFactory();
    }

    public ReceiptExternalStreamedBE uploadFileToAltinn(String fileReference, String file, DataHandler dataHandler) throws IBrokerServiceExternalStreamedUploadFileStreamedAltinnFaultFaultFaultMessage {
        List<Header> headerList = new ArrayList<>();
        Header reportee = null;
        Header reference = null;
        Header filename = null;
        try {
            reportee = new Header(new QName("http://www.altinn.no/services/ServiceEngine/Broker/2015/06", "Reportee"), REPORTEE, new JAXBDataBinding(String.class));
            reference = new Header(new QName("http://www.altinn.no/services/ServiceEngine/Broker/2015/06", "Reference"), fileReference, new JAXBDataBinding(String.class));
            filename = new Header(new QName("http://www.altinn.no/services/ServiceEngine/Broker/2015/06", "FileName"), file, new JAXBDataBinding(String.class));
        } catch (JAXBException e) {
            log.error("Feil i uploadFileToAltinn:", e);
        }
        headerList.add(reportee);
        headerList.add(reference);
        headerList.add(filename);

        ((BindingProvider) brokerServiceExternalStreamed).getRequestContext().put(Header.HEADER_LIST, headerList);
        StreamedPayloadExternalBE streamedPayloadExternalBE = objectFactory.createStreamedPayloadExternalBE();
        streamedPayloadExternalBE.setDataStream(dataHandler);
        return brokerServiceExternalStreamed.uploadFileStreamed(streamedPayloadExternalBE);
    }
}
