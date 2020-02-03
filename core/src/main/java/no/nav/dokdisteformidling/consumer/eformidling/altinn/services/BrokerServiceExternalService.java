package no.nav.dokdisteformidling.consumer.eformidling.altinn.services;

import lombok.extern.slf4j.Slf4j;
import no.altinn.brokerserviceexternal.*;
import no.nav.dokdisteformidling.config.props.DpoUserProperties;
import no.nav.dokdisteformidling.consumer.eformidling.EformidlingConstants;
import no.nav.dokdisteformidling.consumer.eformidling.altinn.mapper.ManifestBuilder;
import no.nav.dokdisteformidling.consumer.eformidling.altinn.to.*;
import no.nav.dokdisteformidling.exception.technical.AltinnBrokerServiceWsException;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static no.nav.dokdisteformidling.common.FunctionalUtils.convertLocalDateTimeToXmlGregorianCalendar;
import static no.nav.dokdisteformidling.consumer.eformidling.EformidlingConstants.NAV_ORGNUMMER;
import static no.nav.dokdisteformidling.consumer.eformidling.altinn.to.AltinnReasonFactory.from;

@Slf4j
@Component
public class BrokerServiceExternalService {


    private static final String FILE_NAME = "sbd.zip";
    private static final String AVAILABLE_FILES_ERROR_MESSAGE = "Det fant ikke filer fra Altinn: {}";
    private static final String FAILED_TO_INITATE_ALTINN_BROKER_SERVICE = "Failed to initate Altinn broker service: {}";
    private static final String CANNOT_DOWNLOAD_FILE = "Kan ikke laste ned filen: {}";
    private static final String NEDLASTING_KAN_IKKE_BEKREFTE = "Nedlastingen kan ikke  bekrefte: {}";
    private static final String ALTINN_TESTKALL_FEILET = "Testkall mot altinn feilet.";

    private final IBrokerServiceExternal brokerServiceExternal;
    private final ObjectFactory objectFactory;
    private final DpoUserProperties dpoUserProperties;

    @Inject
    public BrokerServiceExternalService(final IBrokerServiceExternal brokerServiceExternal,
                                        final DpoUserProperties dpoUserProperties) {
        this.brokerServiceExternal = brokerServiceExternal;
        this.dpoUserProperties = dpoUserProperties;
        this.objectFactory = new ObjectFactory();
    }

    public String intiateBrokerService(UploadManifest uploadManifest) {
        try {
            return brokerServiceExternal.initiateBrokerService(getBrokerServiceInitiation(uploadManifest));
        } catch (IBrokerServiceExternalInitiateBrokerServiceAltinnFaultFaultFaultMessage e) {
            log.error(FAILED_TO_INITATE_ALTINN_BROKER_SERVICE, from(e));
            throw new AltinnBrokerServiceWsException(FAILED_TO_INITATE_ALTINN_BROKER_SERVICE, AltinnReasonFactory.from(e), e);
        }
    }


    protected Optional<BrokerServiceAvailableFileList> getFileReferences(SearchCriteria criteria, ServiceCode serviceCode) {
        try {
            return Optional.of(brokerServiceExternal.getAvailableFiles(getBrokerServiceSearch(NAV_ORGNUMMER, serviceCode, criteria)));
        } catch (IBrokerServiceExternalGetAvailableFilesAltinnFaultFaultFaultMessage e) {
            log.error(AVAILABLE_FILES_ERROR_MESSAGE, from(e));
            throw new AltinnBrokerServiceWsException(AVAILABLE_FILES_ERROR_MESSAGE, AltinnReasonFactory.from(e), e);
        }

    }

    public void confirmDownloaded(String fileReference) {
        try {
            brokerServiceExternal.confirmDownloaded(fileReference, NAV_ORGNUMMER);
        } catch (IBrokerServiceExternalConfirmDownloadedAltinnFaultFaultFaultMessage e) {
            log.error(NEDLASTING_KAN_IKKE_BEKREFTE, from(e));
            throw new AltinnBrokerServiceWsException(NEDLASTING_KAN_IKKE_BEKREFTE, AltinnReasonFactory.from(e), e);
        }

    }

    public List<FileReference> getAvailableFiles(SearchCriteria criteria, ServiceCode serviceCode) {
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
        brokerServiceInitiation.setRecipientList(getArrayOfRecipient(EformidlingConstants.TRYGDERETTEN_ORGNUMMER));
        return brokerServiceInitiation;
    }


    private Manifest getManifest(UploadManifest uploadManifest) {

        return new ManifestBuilder()
                .withSender(uploadManifest.getAvsender())
                .withExternalServiceCode(uploadManifest.getServiceCode())
                .withExternalServiceEditionCode(Integer.valueOf(uploadManifest.getServiceEditionCode()))
                .withFilename(uploadManifest.getFileZipName())
                .withSenderReference(uploadManifest.getSenderReference())
                .build();

    }

    private ArrayOfFile getArrayOfFile(UploadManifest uploadManifest) {
        ArrayOfFile arrayOfFile = objectFactory.createArrayOfFile();
        uploadManifest.getFiles().forEach(fil -> {
            File file = objectFactory.createFile();
            file.setFileName(fil);
            arrayOfFile.getFile().add(file);

        });
        return arrayOfFile;
    }

    private ArrayOfRecipient getArrayOfRecipient(String orgnr) {
        ArrayOfRecipient arrayOfRecipient = objectFactory.createArrayOfRecipient();
        Recipient recipient = objectFactory.createRecipient();
        recipient.setPartyNumber(orgnr);
        return arrayOfRecipient;
    }


    private BrokerServiceSearch getBrokerServiceSearch(String orgnr, ServiceCode serviceCode, SearchCriteria criteria) {
        BrokerServiceSearch brokerServiceSearch = new BrokerServiceSearch();
        brokerServiceSearch.setFileStatus(criteria.getAvailableFileStatus());
        brokerServiceSearch.setReportee(orgnr);
        ObjectFactory objectFactory = new ObjectFactory();
        brokerServiceSearch.setExternalServiceCode(objectFactory.createBrokerServiceSearchExternalServiceCode(serviceCode.getServiceCode()));
        brokerServiceSearch.setExternalServiceEditionCode(Integer.valueOf(serviceCode.getServiceEditionCode()));
        brokerServiceSearch.setMinSentDateTime(convertLocalDateTimeToXmlGregorianCalendar(criteria.getMinSentDate()));
        brokerServiceSearch.setMaxSentDateTime(convertLocalDateTimeToXmlGregorianCalendar(criteria.getMaxSentDate()));
        return brokerServiceSearch;

    }

}
