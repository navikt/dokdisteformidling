package no.nav.dokdisteformidling.consumer.eformidling;


import jakarta.activation.DataHandler;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokdisteformidling.certificate.AppCertificate;
import no.nav.dokdisteformidling.consumer.eformidling.altinn.from.AltinnDokument;
import no.nav.dokdisteformidling.consumer.eformidling.altinn.from.DownloadResponse;
import no.nav.dokdisteformidling.consumer.eformidling.altinn.from.DownloadedMessageFromAltinn;
import no.nav.dokdisteformidling.consumer.eformidling.altinn.mapper.InputStreamDataSource;
import no.nav.dokdisteformidling.consumer.eformidling.altinn.services.BrokerServiceExternalService;
import no.nav.dokdisteformidling.consumer.eformidling.altinn.services.BrokerServiceExternalStreamedService;
import no.nav.dokdisteformidling.consumer.eformidling.altinn.to.ReceiptTo;
import no.nav.dokdisteformidling.consumer.eformidling.altinn.to.SearchCriteria;
import no.nav.dokdisteformidling.consumer.eformidling.altinn.to.ServiceCode;
import no.nav.dokdisteformidling.consumer.eformidling.altinn.to.UploadManifest;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.EformidlingMessagePackager;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.EformidlingMessageUnpackager;
import no.nav.dokdisteformidling.consumer.eformidling.serviceregistry.EformidlingMottakerInfoService;
import no.nav.dokdisteformidling.consumer.eformidling.serviceregistry.MottakerInfo;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static no.altinn.brokerserviceexternal.BrokerServiceAvailableFileStatus.UPLOADED;
import static no.nav.dokdisteformidling.consumer.eformidling.EformidlingConstants.NAV_ORGNUMMER;

@Component
@Slf4j
public class AltinnEformidling implements Eformidling {

    private final AppCertificate appCertificate;
    private final EformidlingMottakerInfoService eformidlingMottakerInfoService;
    private final EformidlingMessagePackager eformidlingMessagePackager;
    private final EformidlingMessageUnpackager eformidlingMessageUnpackager;
    private final BrokerServiceExternalService brokerServiceExternalService;
    private final BrokerServiceExternalStreamedService brokerServiceExternalStreamedService;
    private static final String FILE_NAME = "sbd.zip";

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
    public void send(NavDokumentpakke navDokumentpakke, String avtaltmelding) {
        log.info("Henter mottakerInfo for Trygderetten. conversationId={}, bestillingsId={}", navDokumentpakke.getConversationId(), navDokumentpakke.getBestillingsId());
        final MottakerInfo mottakerInfo = eformidlingMottakerInfoService.hentMottakerInfoTrygderetten();

        log.info("Hentet mottakerInfo={} for Trygderetten. conversationId={}, bestillingsId={}", mottakerInfo, navDokumentpakke.getConversationId(), navDokumentpakke.getBestillingsId());
        final InputStream sbdZip = eformidlingMessagePackager.packageMessage(navDokumentpakke, avtaltmelding, appCertificate, mottakerInfo.getX509Certificate());

        final UploadManifest uploadManifest = mapUploadManifest(mottakerInfo, navDokumentpakke.getConversationId());

        log.info("Initialiserer Altinn broker med manifest={}, conversationId={}, bestillingsId={}", uploadManifest, navDokumentpakke.getConversationId(), navDokumentpakke.getBestillingsId());
        final String fileReference = brokerServiceExternalService.intiateBrokerService(uploadManifest);
        log.info("Altinn broker Initialisert OK. fileReference={}, conversationId={}, bestillingsId={}", fileReference, navDokumentpakke.getConversationId(), navDokumentpakke.getBestillingsId());

        log.info("Laster opp til Altinn fileReference={}, conversationId={}, bestillingsId={}", fileReference, navDokumentpakke.getConversationId(), navDokumentpakke.getBestillingsId());
        ReceiptTo receiptTo = brokerServiceExternalStreamedService.uploadFileToAltinn(fileReference, FILE_NAME, new DataHandler(InputStreamDataSource.of(sbdZip)));
        log.info("Lastet opp OK. receipt={}, conversationId={}, bestillingsId={}", receiptTo, navDokumentpakke.getConversationId(), navDokumentpakke.getBestillingsId());
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

    @Override
    public List<DownloadResponse> hent() {
        final ServiceCode serviceCode = getServiceCode();
        log.info("Henter filreferanser til meldinger fra Trygderetten som kan lastes ned gjennom Altinns formidlingstjeneste på serviceCode={}", serviceCode);
        List<String> filreferanser = brokerServiceExternalService.getAvailableFiles(getSearchCriteria(), serviceCode);
        log.info("Hentet {} filreferanser fra Altinn, referanser={}", filreferanser.size(), filreferanser);

        log.info("Henter meldinger fra Altinn");
        List<DownloadedMessageFromAltinn> messagesFromAltinn = brokerServiceExternalStreamedService.downloadFilesFromAltinn(filreferanser);
        log.info("Hentet {} meldinger fra Altinn, referanser={}", messagesFromAltinn.size(), messagesFromAltinn.stream().map(DownloadedMessageFromAltinn::getFilreferanse).toList());

        log.info("Pakker ut meldinger fra Altinn");
        List<AltinnDokument> altinnDokuments = eformidlingMessageUnpackager.unpackageMessages(messagesFromAltinn);
		log.info("Pakket ut {} meldinger fra Altinn: {}",
				altinnDokuments.size(),
				altinnDokuments.stream()
						.map(altinndokument ->
								String.format("referanse=%s:MessageChannel=%s", altinndokument.getFileReference(), altinndokument.getTrygderettenMelding().getMessageChannelName()))
						.toList());


        List<DownloadResponse> downloadResponses = getDownloadResponses(altinnDokuments);
        log.info("Meldinger fra Altinn={}", downloadResponses);

        return downloadResponses;

    }

    private SearchCriteria getSearchCriteria() {
        return SearchCriteria.builder()
                .availableFileStatus(UPLOADED)
                .build();
    }

    public ServiceCode getServiceCode() {
        MottakerInfo mottakerInfo = eformidlingMottakerInfoService.hentMottakerInfoTrygderetten();
        return ServiceCode.builder()
                .serviceCode(mottakerInfo.getServiceCode())
                .serviceEditionCode(Integer.parseInt(mottakerInfo.getServiceEditionCode()))
                .build();
    }

    private List<DownloadResponse> getDownloadResponses(List<AltinnDokument> altinnDokuments) {
        return altinnDokuments.stream().map(DownloadResponse::from).collect(toList());
    }

    @Override
    public void bekreft(String filreferanse) {
       brokerServiceExternalService.confirmDownloaded(filreferanse);
    }
}
