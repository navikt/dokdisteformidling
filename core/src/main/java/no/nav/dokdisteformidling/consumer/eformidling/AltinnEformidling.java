package no.nav.dokdisteformidling.consumer.eformidling;


import lombok.extern.slf4j.Slf4j;
import no.altinn.brokerserviceexternal.BrokerServiceAvailableFileStatus;
import no.nav.dokdisteformidling.certificate.AppCertificate;
import no.nav.dokdisteformidling.consumer.eformidling.altinn.mapper.InputStreamDataSource;
import no.nav.dokdisteformidling.consumer.eformidling.altinn.services.BrokerServiceExternalService;
import no.nav.dokdisteformidling.consumer.eformidling.altinn.services.BrokerServiceExternalStreamedService;
import no.nav.dokdisteformidling.consumer.eformidling.altinn.to.*;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.EformidlingMessagePackager;
import no.nav.dokdisteformidling.consumer.eformidling.serviceregistry.EformidlingMottakerInfoService;
import no.nav.dokdisteformidling.consumer.eformidling.serviceregistry.MottakerInfo;
import org.springframework.stereotype.Component;

import javax.activation.DataHandler;
import javax.inject.Inject;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.EformidlingMessagePackager.EFORMIDLING_ASIC;
import static no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.EformidlingMessagePackager.EFORMIDLING_SBD;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
@Slf4j
class AltinnEformidling implements Eformidling {
	private final AppCertificate appCertificate;
	private final EformidlingMottakerInfoService eformidlingMottakerInfoService;
	private final EformidlingMessagePackager eformidlingMessagePackager;
	private final BrokerServiceExternalService brokerServiceExternalService;
	private final BrokerServiceExternalStreamedService brokerServiceExternalStreamedService;
	private static final String FILE_NAME = "sbd.zip";


	@Inject
	AltinnEformidling(AppCertificate appCertificate,
					  EformidlingMottakerInfoService eformidlingMottakerInfoService,
					  EformidlingMessagePackager eformidlingMessagePackager,
					  BrokerServiceExternalService brokerServiceExternalService,
					  BrokerServiceExternalStreamedService brokerServiceExternalStreamedService) {
		this.appCertificate = appCertificate;
		this.eformidlingMottakerInfoService = eformidlingMottakerInfoService;
		this.eformidlingMessagePackager = eformidlingMessagePackager;
		this.brokerServiceExternalService = brokerServiceExternalService;
		this.brokerServiceExternalStreamedService = brokerServiceExternalStreamedService;
	}

	@Override
	public UploadResponse send(NavDokumentpakke navDokumentpakke) {
		//TODO: Logging av filstørrelsen på dokumentpakken, metrikker som teller antall kall og timing
		log.info("Henter mottakerInfo for Trygderetten. conversationId={}, bestillingsId={}", navDokumentpakke.getConversationId(), navDokumentpakke
				.getBestillingsId());
		final MottakerInfo mottakerInfo = eformidlingMottakerInfoService.hentMottakerInfoTrygderetten();
		log.info("Hentet mottakerInfo={} for Trygderetten. conversationId={}, bestillingsId={}", mottakerInfo, navDokumentpakke.getConversationId(), navDokumentpakke
				.getBestillingsId());
		final InputStream sbdZip = eformidlingMessagePackager.packageMessage(navDokumentpakke, appCertificate,
				mottakerInfo.getX509Certificate());

		log.info("Initialiserer Altinn broker med conversationId={}, bestillingsId={}", navDokumentpakke.getConversationId(), navDokumentpakke
				.getBestillingsId());
		String fileReference = brokerServiceExternalService.intiateBrokerService(mapUploadManifest(Arrays.asList(EFORMIDLING_SBD, EFORMIDLING_ASIC),
				navDokumentpakke.getConversationId()));
		log.info("Altinn broker Initialisert OK. fileReference={}, conversationId={}, bestillingsId={}", fileReference, navDokumentpakke
				.getConversationId(), navDokumentpakke.getBestillingsId());

		log.info("Laster opp til Altinn conversationId={}, bestillingsId={}", navDokumentpakke.getConversationId(), navDokumentpakke
				.getBestillingsId());
		ReceiptTo receiptTo = brokerServiceExternalStreamedService.uploadFileToAltinn(fileReference, FILE_NAME, new DataHandler(InputStreamDataSource
				.of(sbdZip)));
		log.info("Lastet opp OK. receipt={}, conversationId={}, bestillingsId={}", receiptTo, navDokumentpakke.getConversationId(), navDokumentpakke
				.getBestillingsId());

		return UploadResponse.builder()
				.fileReference(fileReference)
				.receiptTo(receiptTo)
				.build();
	}


	public UploadManifest mapUploadManifest(List<String> fileList, String senderReference) {
		MottakerInfo mottakerInfo = eformidlingMottakerInfoService.hentMottakerInfoTrygderetten();
		return UploadManifest.builder()
				.avsender(EformidlingConstants.NAV_ORGNUMMER)
				.serviceCode(mottakerInfo.getServiceCode())
				.serviceEditionCode(mottakerInfo.getServiceEditionCode())
				.fileZipName(FILE_NAME)
				.files(fileList)
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
	public DownloadResponse hent() {
		log.info("Henter tilgjengelige filer til NAV fra Trygderetten gjennom formidlingstjenesten");
		List<FileReference> availableFiles = brokerServiceExternalService.getAvailableFiles(getSearchCriteria(), getServiceCode());

		DownloadResponse response = brokerServiceExternalStreamedService.downloadFilesFromAltinn(availableFiles);
		confirmDownloaded(response);
		return response;
	}

	private SearchCriteria getSearchCriteria() {
		return SearchCriteria.builder()
				.availableFileStatus(BrokerServiceAvailableFileStatus.UPLOADED)
				.minSentDate(LocalDateTime.MIN)   //TODO: Fiks riktige verdier
				.maxSentDate(LocalDateTime.MAX)   //TODO: Fiks riktige verdier
				.build();
	}

	private void confirmDownloaded(DownloadResponse downloadResponse) {
		downloadResponse.getDownloadedFileFromAltinn().forEach(downloadedFileFromAltinn ->
				brokerServiceExternalService.confirmDownloaded(downloadedFileFromAltinn.getFileReference().getFileReference()));
	}
}
