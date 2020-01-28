package no.nav.dokdisteformidling.consumer.eformidling.brokerserviceexternalec2.to;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import no.altinn.brokerserviceexternaec2.ArrayOfFile;
import no.altinn.brokerserviceexternaec2.ArrayOfProperty;
import no.altinn.brokerserviceexternaec2.Recipient;

import javax.xml.bind.JAXBElement;
import java.util.List;

@Getter
@Setter
@Builder
public class BrokerServiceInitiationTo {

    private ManifestTo manifestTo;
    private ArrayOfRecipientTo recipientList;


    @Setter
    @Getter
    @Builder
    public static class ManifestTo {
        private String externalServiceCode;
        private int externalServiceEditionCode;
        private JAXBElement<ArrayOfFile> fileList;
        private JAXBElement<ArrayOfProperty> propertyList;
        private String reportee; //nav-organizationNummer
        private String sendersReference; //CONVERSATION_ID

    }

    @Getter
    @Setter
    @Builder
    public class ArrayOfRecipientTo {
        private List<Recipient> recipient;
    }
}
