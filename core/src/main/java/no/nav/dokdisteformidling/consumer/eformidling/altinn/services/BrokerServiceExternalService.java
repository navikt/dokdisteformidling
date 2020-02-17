package no.nav.dokdisteformidling.consumer.eformidling.altinn.services;

import lombok.extern.slf4j.Slf4j;
import no.altinn.brokerserviceexternal.ArrayOfRecipient;
import no.altinn.brokerserviceexternal.BrokerServiceAvailableFileList;
import no.altinn.brokerserviceexternal.BrokerServiceInitiation;
import no.altinn.brokerserviceexternal.BrokerServiceSearch;
import no.altinn.brokerserviceexternal.IBrokerServiceExternal;
import no.altinn.brokerserviceexternal.IBrokerServiceExternalConfirmDownloadedAltinnFaultFaultFaultMessage;
import no.altinn.brokerserviceexternal.IBrokerServiceExternalGetAvailableFilesAltinnFaultFaultFaultMessage;
import no.altinn.brokerserviceexternal.IBrokerServiceExternalInitiateBrokerServiceAltinnFaultFaultFaultMessage;
import no.altinn.brokerserviceexternal.IBrokerServiceExternalTestAltinnFaultFaultFaultMessage;
import no.altinn.brokerserviceexternal.Manifest;
import no.altinn.brokerserviceexternal.ObjectFactory;
import no.altinn.brokerserviceexternal.Recipient;
import no.nav.dokdisteformidling.consumer.eformidling.altinn.mapper.ManifestBuilder;
import no.nav.dokdisteformidling.consumer.eformidling.altinn.to.AltinnReasonFactory;
import no.nav.dokdisteformidling.consumer.eformidling.altinn.to.FileReference;
import no.nav.dokdisteformidling.consumer.eformidling.altinn.to.SearchCriteria;
import no.nav.dokdisteformidling.consumer.eformidling.altinn.to.ServiceCode;
import no.nav.dokdisteformidling.consumer.eformidling.altinn.to.UploadManifest;
import no.nav.dokdisteformidling.exception.technical.AltinnBrokerServiceWsException;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static no.nav.dokdisteformidling.utils.DateConverterUtil.convertLocalDateTimeToXmlGregorianCalendar;
import static no.nav.dokdisteformidling.consumer.eformidling.EformidlingConstants.NAV_ORGNUMMER;
import static no.nav.dokdisteformidling.consumer.eformidling.EformidlingConstants.TRYGDERETTEN_ORGNUMMER;

@Slf4j
@Component
public class BrokerServiceExternalService {
    private static final String INITIATE_BROKER_SERVICE_FEILET = "Kall til BrokerService.initiateBrokerService feilet.";
    private static final String GET_AVAILABLE_FILES_FEILET = "Kall til BrokerService.getAvailableFiles feilet.";
    private static final String CONFIRM_DOWNLOADED_FEILET = "Kall til BrokerService.confirmDownloaded feilet";
    private static final String ALTINN_TESTKALL_FEILET = "Testkall mot altinn feilet.";

    private final IBrokerServiceExternal brokerServiceExternal;
    private final ObjectFactory objectFactory;

    @Inject
    public BrokerServiceExternalService(final IBrokerServiceExternal brokerServiceExternal) {
        this.brokerServiceExternal = brokerServiceExternal;
        this.objectFactory = new ObjectFactory();
    }

    public String intiateBrokerService(UploadManifest uploadManifest) {
        try {
            return brokerServiceExternal.initiateBrokerService(getBrokerServiceInitiation(uploadManifest));
        } catch (IBrokerServiceExternalInitiateBrokerServiceAltinnFaultFaultFaultMessage e) {
            throw new AltinnBrokerServiceWsException(INITIATE_BROKER_SERVICE_FEILET, AltinnReasonFactory.from(e), e);
        }
    }


    protected Optional<BrokerServiceAvailableFileList> getFileReferences(SearchCriteria criteria, ServiceCode serviceCode) {
        try {
            return Optional.of(brokerServiceExternal.getAvailableFiles(getBrokerServiceSearch(NAV_ORGNUMMER, serviceCode, criteria)));
        } catch (IBrokerServiceExternalGetAvailableFilesAltinnFaultFaultFaultMessage e) {
            throw new AltinnBrokerServiceWsException(GET_AVAILABLE_FILES_FEILET, AltinnReasonFactory.from(e), e);
        }
    }

    public void confirmDownloaded(String fileReference) {
        try {
            brokerServiceExternal.confirmDownloaded(fileReference, NAV_ORGNUMMER);
            log.info("Det bekreftet at fil med fileReference: " + fileReference + "nedlastet");
        } catch (IBrokerServiceExternalConfirmDownloadedAltinnFaultFaultFaultMessage e) {
            log.error(String.format("%s med fileReference:%s",CONFIRM_DOWNLOADED_FEILET,fileReference));
            throw new AltinnBrokerServiceWsException(CONFIRM_DOWNLOADED_FEILET, AltinnReasonFactory.from(e), e);
        }
    }

    public List<FileReference> getAvailableFiles(SearchCriteria criteria, ServiceCode serviceCode) {
        //TODO: Metrics and logging, number of files available for download, files + filerefence?
        return getFileReferences(criteria, serviceCode)
                .map(BrokerServiceAvailableFileList::getBrokerServiceAvailableFile)
                .orElse(Collections.emptyList())
                .stream()
                .map(file -> new FileReference(file.getFileReference(), file.getReceiptID()))
                .collect(Collectors.toList());
    }

    public void test() {
        try {
            brokerServiceExternal.test();
        } catch (IBrokerServiceExternalTestAltinnFaultFaultFaultMessage e) {
            log.error(ALTINN_TESTKALL_FEILET, e);
            throw new AltinnBrokerServiceWsException(ALTINN_TESTKALL_FEILET, AltinnReasonFactory.from(e), e);
        }
    }

    private BrokerServiceInitiation getBrokerServiceInitiation(UploadManifest uploadManifest) {
        BrokerServiceInitiation brokerServiceInitiation = objectFactory.createBrokerServiceInitiation();
        brokerServiceInitiation.setManifest(getManifest(uploadManifest));
        brokerServiceInitiation.setRecipientList(getArrayOfRecipient(TRYGDERETTEN_ORGNUMMER));
        return brokerServiceInitiation;
    }

    private Manifest getManifest(UploadManifest uploadManifest) {
        return new ManifestBuilder()
                .withSender(uploadManifest.getAvsender())
                .withExternalServiceCode(uploadManifest.getServiceCode())
                .withExternalServiceEditionCode(Integer.parseInt(uploadManifest.getServiceEditionCode()))
                .withFilename(uploadManifest.getFileZipName())
                .withSenderReference(uploadManifest.getSenderReference())
                .build();
    }

    private ArrayOfRecipient getArrayOfRecipient(String orgnr) {
        ArrayOfRecipient arrayOfRecipient = objectFactory.createArrayOfRecipient();
        Recipient recipient = objectFactory.createRecipient();
        recipient.setPartyNumber(orgnr);
        arrayOfRecipient.getRecipient().add(recipient);
        return arrayOfRecipient;
    }

    private BrokerServiceSearch getBrokerServiceSearch(String orgnr, ServiceCode serviceCode, SearchCriteria criteria) {
        BrokerServiceSearch brokerServiceSearch = new BrokerServiceSearch();
        brokerServiceSearch.setFileStatus(criteria.getAvailableFileStatus());
        brokerServiceSearch.setReportee(orgnr);
        ObjectFactory objectFactory = new ObjectFactory();
        brokerServiceSearch.setExternalServiceCode(objectFactory.createBrokerServiceSearchExternalServiceCode(serviceCode.getServiceCode()));
        brokerServiceSearch.setExternalServiceEditionCode(serviceCode.getServiceEditionCode());
        brokerServiceSearch.setMinSentDateTime(criteria.getMinSentDate() == null ? null : convertLocalDateTimeToXmlGregorianCalendar(criteria.getMinSentDate()));
        brokerServiceSearch.setMaxSentDateTime(criteria.getMaxSentDate() == null ? null : convertLocalDateTimeToXmlGregorianCalendar(criteria.getMaxSentDate()));
        return brokerServiceSearch;
    }
}
