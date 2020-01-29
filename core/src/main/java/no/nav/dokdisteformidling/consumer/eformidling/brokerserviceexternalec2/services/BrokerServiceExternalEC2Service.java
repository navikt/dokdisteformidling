package no.nav.dokdisteformidling.consumer.eformidling.brokerserviceexternalec2.services;

import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.marker.LogstashMarker;
import net.logstash.logback.marker.Markers;
import no.altinn.brokerserviceexternaec2.*;
import no.altinn.brokerserviceexternalec2streamed.ReceiptExternalStreamedBE;
import no.nav.dokdisteformidling.certificate.SecurityCredential;
import no.nav.dokdisteformidling.config.props.DpoUserProperties;
import no.nav.dokdisteformidling.consumer.eformidling.EformidlingConstants;
import no.nav.dokdisteformidling.consumer.eformidling.brokerserviceexternalec2.AltinnBrokerServiceConsumerFactory;
import no.nav.dokdisteformidling.consumer.eformidling.brokerserviceexternalec2.to.AltinnResonFactory;
import no.nav.dokdisteformidling.consumer.eformidling.brokerserviceexternalec2.to.SearchCriteria;
import no.nav.dokdisteformidling.consumer.eformidling.brokerserviceexternalec2.to.UploadManifest;
import no.nav.dokdisteformidling.consumer.eformidling.serviceregistry.MottakerInfo;
import no.nav.dokdisteformidling.utils.DateConverterUtil;

import javax.xml.datatype.DatatypeConfigurationException;
import java.util.Optional;
import java.util.stream.IntStream;

import static no.nav.dokdisteformidling.consumer.eformidling.EformidlingConstants.NAV_ORGNUMMER;

@Slf4j
public class BrokerServiceExternalEC2Service {


    private static final String FILE_NAME = "sbd.zip";
    private static final String AVAILABLE_FILES_ERROR_MESSAGE = "Det fant ikke filer fra Altinn";

    private final IBrokerServiceExternalEC2 iBrokerServiceExternalEC2;
    private final ObjectFactory objectFactory;
    private final SecurityCredential credential;
    private final DpoUserProperties dpoUserProperties;

    public BrokerServiceExternalEC2Service(final AltinnBrokerServiceConsumerFactory brokerServiceConsumerFactory,
                                           SecurityCredential credential) {
        this.credential = credential;
        this.iBrokerServiceExternalEC2 = brokerServiceConsumerFactory.getBrokerServiceClient(credential);
        this.objectFactory = new ObjectFactory();
        this.dpoUserProperties = credential.getDpoUserProperties();

    }

    public void intiateBrokerService(UploadManifest uploadManifest) throws IBrokerServiceExternalEC2InitiateBrokerServiceECAltinnFaultFaultFaultMessage {
        DpoUserProperties dpoUserProperties = credential.getDpoUserProperties();
        iBrokerServiceExternalEC2.initiateBrokerServiceEC(dpoUserProperties.getUsername(),
                dpoUserProperties.getPassword(),
                getBrokerServiceInitiation(uploadManifest));
    }


    protected Optional<BrokerServiceAvailableFileList> getFileReferences(SearchCriteria criteria, MottakerInfo mottakerInfo) throws DatatypeConfigurationException, IBrokerServiceExternalEC2GetAvailableFilesECAltinnFaultFaultFaultMessage {
        try {
            return Optional.of(iBrokerServiceExternalEC2.getAvailableFilesEC(dpoUserProperties.getUsername(),
                    dpoUserProperties.getPassword(),
                    getBrokerServiceSearch(NAV_ORGNUMMER, mottakerInfo, criteria)));
        } catch (IBrokerServiceExternalEC2GetAvailableFilesECAltinnFaultFaultFaultMessage e) {
            log.error("", AltinnResonFactory.from(e));
            return Optional.empty();
        }

    }

    protected void confirmDownloaded(String fileReference) throws IBrokerServiceExternalEC2ConfirmDownloadedECAltinnFaultFaultFaultMessage {
        iBrokerServiceExternalEC2.confirmDownloadedEC(dpoUserProperties.getUsername(), dpoUserProperties.getPassword(), fileReference, NAV_ORGNUMMER);
    }

    public BrokerServiceAvailableFileList getAvailableFiles(SearchCriteria criteria, MottakerInfo mottakerInfo) throws DatatypeConfigurationException, IBrokerServiceExternalEC2GetAvailableFilesECAltinnFaultFaultFaultMessage {
        return getFileReferences(criteria, mottakerInfo).get();
    }


    private BrokerServiceInitiation getBrokerServiceInitiation(UploadManifest uploadManifest) {
        BrokerServiceInitiation brokerServiceInitiation = objectFactory.createBrokerServiceInitiation();
        brokerServiceInitiation.setManifest(getManifest(uploadManifest));
        brokerServiceInitiation.setRecipientList(getArrayOfRecipient(EformidlingConstants.TRYGDERETTEN_ORGNUMMER));
        return brokerServiceInitiation;
    }


    private Manifest getManifest(UploadManifest uploadManifest) {
        Manifest manifest = objectFactory.createManifest();
        manifest.setExternalServiceCode(uploadManifest.getMottakerInfo().getServiceCode());
        manifest.setExternalServiceEditionCode(Integer.valueOf(uploadManifest.getMottakerInfo().getServiceEditionCode()));
        manifest.setReportee(NAV_ORGNUMMER);
        manifest.setSendersReference(uploadManifest.getSenderReference());
        manifest.setFileList(objectFactory.createManifestFileList(getArrayOfFile(uploadManifest)));
        ArrayOfProperty arrayOfProperty = objectFactory.createArrayOfProperty();

        if (uploadManifest.getProperties() != null) {
            uploadManifest.getProperties().entrySet().stream().forEach(entry -> {
                Property property = objectFactory.createProperty();
                property.setPropertyKey(entry.getKey());
                property.setPropertyValue(entry.getValue());
                arrayOfProperty.getProperty().add(property);

            });
        }
        return manifest;
    }

    private ArrayOfFile getArrayOfFile(UploadManifest uploadManifest) {
        ArrayOfFile arrayOfFile = objectFactory.createArrayOfFile();
        IntStream.range(0, uploadManifest.getFiles().size()).forEach(index -> {
            File file = objectFactory.createFile();
            file.setFileName(uploadManifest.getFiles().get(index));
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


    private BrokerServiceSearch getBrokerServiceSearch(String orgnr, MottakerInfo mottakerInfo, SearchCriteria criteria) throws DatatypeConfigurationException {
        BrokerServiceSearch brokerServiceSearch = new BrokerServiceSearch();
        brokerServiceSearch.setFileStatus(BrokerServiceAvailableFileStatus.UPLOADED);
        brokerServiceSearch.setReportee(orgnr);
        ObjectFactory objectFactory = new ObjectFactory();
        brokerServiceSearch.setExternalServiceCode(objectFactory.createBrokerServiceSearchExternalServiceCode(mottakerInfo.getServiceCode()));
        brokerServiceSearch.setExternalServiceEditionCode(Integer.valueOf(mottakerInfo.getServiceEditionCode()));
        brokerServiceSearch.setReportee(orgnr);
        brokerServiceSearch.setFileStatus(criteria.getAvailableFileStatus());
        brokerServiceSearch.setMinSentDateTime(DateConverterUtil.convertToXMLGregorianCalendar(criteria.getMinSentDate()));
        brokerServiceSearch.setMaxSentDateTime(DateConverterUtil.convertToXMLGregorianCalendar(criteria.getMaxSentDate()));
        return brokerServiceSearch;

    }


    private LogstashMarker markerForm(ReceiptExternalStreamedBE receiptAltinn) {
        LogstashMarker idMarker = Markers.append("altinn-mottaker-id", receiptAltinn.getReceiptId());
        LogstashMarker statusCodeMarker = Markers.append("altinn-status-code", receiptAltinn.getReceiptStatusCode().getValue());
        LogstashMarker textMarker = Markers.append("altinn-text", receiptAltinn.getReceiptText().getValue());
        return idMarker.and(statusCodeMarker).and(textMarker);
    }


}
