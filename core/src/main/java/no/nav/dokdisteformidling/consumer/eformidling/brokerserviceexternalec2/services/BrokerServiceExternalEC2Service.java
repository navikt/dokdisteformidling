package no.nav.dokdisteformidling.consumer.eformidling.brokerserviceexternalec2.services;

import lombok.extern.slf4j.Slf4j;
import no.altinn.brokerserviceexternaec2.*;
import no.nav.dokdisteformidling.certificate.SecurityCredential;
import no.nav.dokdisteformidling.config.props.DpoUserProperties;
import no.nav.dokdisteformidling.consumer.eformidling.EformidlingConstants;
import no.nav.dokdisteformidling.consumer.eformidling.brokerserviceexternalec2.AltinnBrokerServiceConsumerFactory;
import no.nav.dokdisteformidling.consumer.eformidling.brokerserviceexternalec2.to.SearchCriteria;
import no.nav.dokdisteformidling.consumer.eformidling.brokerserviceexternalec2.to.UploadManifest;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.AsiceCreator;
import no.nav.dokdisteformidling.consumer.eformidling.serviceregistry.EformidlingMottakerInfoService;
import no.nav.dokdisteformidling.consumer.eformidling.serviceregistry.MottakerInfo;
import no.nav.dokdisteformidling.utils.DateConverterUtil;
import org.springframework.stereotype.Component;

import javax.xml.datatype.DatatypeConfigurationException;
import java.util.stream.IntStream;

import static no.nav.dokdisteformidling.consumer.eformidling.EformidlingConstants.NAV_ORGNUMMER;

@Slf4j
@Component
public class BrokerServiceExternalEC2Service {

    private final IBrokerServiceExternalEC2 iBrokerServiceExternalEC2;
    private final ObjectFactory objectFactory;
    private final EformidlingMottakerInfoService eformidlingMottakerInfoService;
    private final AltinnBrokerServiceConsumerFactory brokerServiceConsumerFactory;
    private final SecurityCredential credential;
    private AsiceCreator asiceCreator;
    private final DpoUserProperties dpoUserProperties;

    public BrokerServiceExternalEC2Service(AltinnBrokerServiceConsumerFactory brokerServiceConsumerFactory,
                                           EformidlingMottakerInfoService eformidlingMottakerInfoService, DpoUserProperties dpoUserProperties) {
        this.credential = new SecurityCredential();
        this.brokerServiceConsumerFactory = brokerServiceConsumerFactory;
        this.iBrokerServiceExternalEC2 = brokerServiceConsumerFactory.getBrokerServiceExternalClient(credential);
        this.objectFactory = new ObjectFactory();
        this.eformidlingMottakerInfoService = eformidlingMottakerInfoService;
        this.dpoUserProperties = dpoUserProperties;
    }

    protected String intiateBrokerService(UploadManifest uploadManifest) throws IBrokerServiceExternalEC2InitiateBrokerServiceECAltinnFaultFaultFaultMessage {
        DpoUserProperties dpoUserProperties = credential.getDpoUserProperties();
        return iBrokerServiceExternalEC2.initiateBrokerServiceEC(dpoUserProperties.getUser(), dpoUserProperties.getPassword(), getBrokerServiceInitiation(uploadManifest));
    }


    protected BrokerServiceAvailableFileList getFileReferences(SearchCriteria criteria) throws DatatypeConfigurationException, IBrokerServiceExternalEC2GetAvailableFilesECAltinnFaultFaultFaultMessage {
        DpoUserProperties dpoUserProperties = credential.getDpoUserProperties();
        return iBrokerServiceExternalEC2.getAvailableFilesEC(dpoUserProperties.getUser(), dpoUserProperties.getPassword(), getBrokerServiceSearch(NAV_ORGNUMMER, criteria));
    }

    protected void confirmDownloaded(String fileReference) throws IBrokerServiceExternalEC2ConfirmDownloadedECAltinnFaultFaultFaultMessage {
        iBrokerServiceExternalEC2.confirmDownloadedEC(dpoUserProperties.getUser(), dpoUserProperties.getPassword(),fileReference,NAV_ORGNUMMER);
    }

    public BrokerServiceAvailableFileList getAvailableFiles(SearchCriteria criteria) throws DatatypeConfigurationException, IBrokerServiceExternalEC2GetAvailableFilesECAltinnFaultFaultFaultMessage {
        log.info("tilgjengligje filer");
        return getFileReferences(criteria);
    }


    private BrokerServiceInitiation getBrokerServiceInitiation(UploadManifest uploadManifest) {
        BrokerServiceInitiation brokerServiceInitiation = objectFactory.createBrokerServiceInitiation();
        brokerServiceInitiation.setManifest(getManifest(uploadManifest));
        brokerServiceInitiation.setRecipientList(getArrayOfRecipient(EformidlingConstants.TRYGDERETTEN_ORGNUMMER));
        return brokerServiceInitiation;
    }


    private Manifest getManifest(UploadManifest uploadManifest) {
        Manifest manifest = objectFactory.createManifest();
        MottakerInfo mottakerInfo = eformidlingMottakerInfoService.hentMottakerInfoTrygderetten();
        manifest.setExternalServiceCode(mottakerInfo.getServiceCode());
        manifest.setExternalServiceEditionCode(Integer.valueOf(mottakerInfo.getServiceEditionCode()));
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

    private ArrayOfRecipient getArrayOfRecipient(String mottakerOrgNummer) {
        ArrayOfRecipient arrayOfRecipient = objectFactory.createArrayOfRecipient();
        Recipient recipient = objectFactory.createRecipient();
        recipient.setPartyNumber(mottakerOrgNummer);
        return arrayOfRecipient;
    }

    private BrokerServiceSearch getBrokerServiceSearch(String enhet, SearchCriteria criteria) throws DatatypeConfigurationException {
        BrokerServiceSearch brokerServiceSearch = objectFactory.createBrokerServiceSearch();
        MottakerInfo mottakerInfo = eformidlingMottakerInfoService.hentMottakerInfoTrygderetten();
        brokerServiceSearch.setExternalServiceCode(objectFactory.createBrokerServiceSearchExternalServiceCode(mottakerInfo.getServiceCode()));
        brokerServiceSearch.setExternalServiceEditionCode(Integer.valueOf(mottakerInfo.getServiceEditionCode()));
        brokerServiceSearch.setReportee(enhet);
        brokerServiceSearch.setFileStatus(criteria.getAvailableFileStatus());
        brokerServiceSearch.setMinSentDateTime(DateConverterUtil.convertToXMLGregorianCalendar(criteria.getMinSentDate()));
        brokerServiceSearch.setMaxSentDateTime(DateConverterUtil.convertToXMLGregorianCalendar(criteria.getMaxSentDAte()));
        return brokerServiceSearch;

    }


}
