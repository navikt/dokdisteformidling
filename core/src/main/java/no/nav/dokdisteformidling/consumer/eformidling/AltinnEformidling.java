package no.nav.dokdisteformidling.consumer.eformidling;


import lombok.extern.slf4j.Slf4j;
import no.altinn.brokerserviceexternal.BrokerServiceAvailableFileStatus;
import no.nav.dokdisteformidling.certificate.AppCertificate;
import no.nav.dokdisteformidling.consumer.eformidling.altinn.from.AltinnDokument;
import no.nav.dokdisteformidling.consumer.eformidling.altinn.from.DownloadResponse;
import no.nav.dokdisteformidling.consumer.eformidling.altinn.from.DownloadedMessageFromAltinn;
import no.nav.dokdisteformidling.consumer.eformidling.altinn.mapper.DownloadResponseBuilder;
import no.nav.dokdisteformidling.consumer.eformidling.altinn.mapper.InputStreamDataSource;
import no.nav.dokdisteformidling.consumer.eformidling.altinn.services.BrokerServiceExternalService;
import no.nav.dokdisteformidling.consumer.eformidling.altinn.services.BrokerServiceExternalStreamedService;
import no.nav.dokdisteformidling.consumer.eformidling.altinn.to.FileReference;
import no.nav.dokdisteformidling.consumer.eformidling.altinn.to.ReceiptTo;
import no.nav.dokdisteformidling.consumer.eformidling.altinn.to.SearchCriteria;
import no.nav.dokdisteformidling.consumer.eformidling.altinn.to.ServiceCode;
import no.nav.dokdisteformidling.consumer.eformidling.altinn.to.UploadManifest;
import no.nav.dokdisteformidling.consumer.eformidling.altinn.to.UploadResponse;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.EformidlingMessagePackager;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.EformidlingMessageUnpackager;
import no.nav.dokdisteformidling.consumer.eformidling.serviceregistry.EformidlingMottakerInfoService;
import no.nav.dokdisteformidling.consumer.eformidling.serviceregistry.MottakerInfo;
import org.springframework.stereotype.Component;

import javax.activation.DataHandler;
import javax.inject.Inject;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

import static no.nav.dokdisteformidling.consumer.eformidling.EformidlingConstants.NAV_ORGNUMMER;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
@Slf4j
class AltinnEformidling implements Eformidling {
    private final AppCertificate appCertificate;
    private final EformidlingMottakerInfoService eformidlingMottakerInfoService;
    private final EformidlingMessagePackager eformidlingMessagePackager;
    private final EformidlingMessageUnpackager eformidlingMessageUnpackager;
    private final BrokerServiceExternalService brokerServiceExternalService;
    private final BrokerServiceExternalStreamedService brokerServiceExternalStreamedService;
    private static final String FILE_NAME = "sbd.zip";

    @Inject
    AltinnEformidling(AppCertificate appCertificate,
                      EformidlingMottakerInfoService eformidlingMottakerInfoService,
                      EformidlingMessagePackager eformidlingMessagePackager,
                      EformidlingMessageUnpackager eformidlingMessageUnpackager, BrokerServiceExternalService brokerServiceExternalService,
                      BrokerServiceExternalStreamedService brokerServiceExternalStreamedService) {
        this.appCertificate = appCertificate;
        this.eformidlingMottakerInfoService = eformidlingMottakerInfoService;
        this.eformidlingMessagePackager = eformidlingMessagePackager;
        this.eformidlingMessageUnpackager = eformidlingMessageUnpackager;
        this.brokerServiceExternalService = brokerServiceExternalService;
        this.brokerServiceExternalStreamedService = brokerServiceExternalStreamedService;
    }

    @Override
    public UploadResponse send(NavDokumentpakke navDokumentpakke) {
        log.info("Henter mottakerInfo for Trygderetten. conversationId={}, bestillingsId={}", navDokumentpakke.getConversationId(), navDokumentpakke.getBestillingsId());
        final MottakerInfo mottakerInfo = eformidlingMottakerInfoService.hentMottakerInfoTrygderetten();
        log.info("Hentet mottakerInfo={} for Trygderetten. conversationId={}, bestillingsId={}", mottakerInfo, navDokumentpakke.getConversationId(), navDokumentpakke.getBestillingsId());
        final InputStream sbdZip = eformidlingMessagePackager.packageMessage(navDokumentpakke, appCertificate,
                mottakerInfo.getX509Certificate());

        final UploadManifest uploadManifest = mapUploadManifest(mottakerInfo, navDokumentpakke.getConversationId());
        log.info("Initialiserer Altinn broker med manifest={}, conversationId={}, bestillingsId={}", uploadManifest, navDokumentpakke.getConversationId(), navDokumentpakke.getBestillingsId());
        final String fileReference = brokerServiceExternalService.intiateBrokerService(uploadManifest);
        log.info("Altinn broker Initialisert OK. fileReference={}, conversationId={}, bestillingsId={}", fileReference, navDokumentpakke.getConversationId(), navDokumentpakke.getBestillingsId());

        log.info("Laster opp til Altinn fileReference={}, conversationId={}, bestillingsId={}", fileReference, navDokumentpakke.getConversationId(), navDokumentpakke.getBestillingsId());
        ReceiptTo receiptTo = brokerServiceExternalStreamedService.uploadFileToAltinn(fileReference, FILE_NAME, new DataHandler(InputStreamDataSource.of(sbdZip)));
        log.info("Lastet opp OK. receipt={}, conversationId={}, bestillingsId={}", receiptTo, navDokumentpakke.getConversationId(), navDokumentpakke.getBestillingsId());

        return UploadResponse.builder()
                .fileReference(fileReference)
                .receiptTo(receiptTo)
                .build();
    }

    UploadManifest mapUploadManifest(final MottakerInfo mottakerInfo, final String senderReference) {
        return UploadManifest.builder()
                .avsender(NAV_ORGNUMMER)
                .serviceCode(mottakerInfo.getServiceCode())
                .serviceEditionCode(mottakerInfo.getServiceEditionCode())
                .fileZipName(FILE_NAME)
                .senderReference(senderReference)
                .build();
    }

    public ServiceCode getServiceCode() {
        MottakerInfo mottakerInfo = eformidlingMottakerInfoService.hentMottakerInfoTrygderetten();
        return ServiceCode.builder()
                .serviceCode(mottakerInfo.getServiceCode())
                .serviceEditionCode(Integer.parseInt(mottakerInfo.getServiceEditionCode()))
                .build();
    }

    @Override
    public void hent() {
        log.info("Henter tilgjengelige filer til NAV fra Trygderetten gjennom formidlingstjenesten");
        List<FileReference> availableFiles = brokerServiceExternalService.getAvailableFiles(getSearchCriteria(), getServiceCode());

        List<DownloadedMessageFromAltinn> messagesFromAltinn = brokerServiceExternalStreamedService.downloadFilesFromAltinn(availableFiles);
        List<AltinnDokument> altinnDokuments = eformidlingMessageUnpackager.unpackageMessages(messagesFromAltinn);
        List<DownloadResponse> downloadResponses = getDownloadResponses(altinnDokuments);
    }

    private SearchCriteria getSearchCriteria() {
        return SearchCriteria.builder()
                .availableFileStatus(BrokerServiceAvailableFileStatus.UPLOADED)
                .build();
    }

    private List<DownloadResponse> getDownloadResponses(List<AltinnDokument> altinnDokuments) {
        return altinnDokuments.stream().map(altinnDokument -> new DownloadResponseBuilder().withAltinnDokument(altinnDokument).build()).collect(Collectors.toList());
    }
}
