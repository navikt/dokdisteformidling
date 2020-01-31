package no.nav.dokdisteformidling.consumer.eformidling;


import lombok.extern.slf4j.Slf4j;
import no.altinn.brokerserviceexternalstreamed.IBrokerServiceExternalStreamedUploadFileStreamedAltinnFaultFaultFaultMessage;
import no.altinn.brokerserviceexternalstreamed.ReceiptExternalStreamedBE;
import no.nav.dokdisteformidling.certificate.AppCertificate;
import no.nav.dokdisteformidling.consumer.eformidling.altinn.mapper.InputStreamDataSource;
import no.nav.dokdisteformidling.consumer.eformidling.altinn.services.BrokerServiceExternalService;
import no.nav.dokdisteformidling.consumer.eformidling.altinn.services.BrokerServiceExternalStreamedService;
import no.nav.dokdisteformidling.consumer.eformidling.altinn.to.ServiceCode;
import no.nav.dokdisteformidling.consumer.eformidling.altinn.to.UploadManifest;
import no.nav.dokdisteformidling.consumer.eformidling.altinn.to.UploadResponse;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.EformidlingMessagePackager;
import no.nav.dokdisteformidling.consumer.eformidling.serviceregistry.EformidlingMottakerInfoService;
import no.nav.dokdisteformidling.consumer.eformidling.serviceregistry.MottakerInfo;
import org.springframework.stereotype.Component;

import javax.activation.DataHandler;
import javax.inject.Inject;
import java.io.InputStream;
import java.util.List;

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
	public UploadResponse send(NavDokumentpakke navDokumentpakke) throws IBrokerServiceExternalStreamedUploadFileStreamedAltinnFaultFaultFaultMessage {
		final MottakerInfo mottakerInfo = eformidlingMottakerInfoService.hentMottakerInfoTrygderetten();
		final InputStream sbdZip = eformidlingMessagePackager.packageMessage(navDokumentpakke,
				appCertificate, mottakerInfo.getX509Certificate());

		log.info("Initializing broker");
//		TODO Altinn BrokerServiceExternal.InitiateBrokerService(navDokumentpakke, mottakerInfo)
//		String fileReference  = brokerServiceExternalService.intiateBrokerService(mapUploadManifest(navDokumentpakke, NAV_REFERANCE));
		String fileReference = "testFileReference";
		log.info("Init ok. Reference = " + fileReference);

		log.info("Uploading file: " + FILE_NAME);
		ReceiptExternalStreamedBE receipt = brokerServiceExternalStreamedService.uploadFileToAltinn(fileReference, FILE_NAME, new DataHandler(InputStreamDataSource.of(sbdZip)));
		log.info("Upload ok. Receipt = " + receipt);

		return new UploadResponse(fileReference, receipt);
	}



	public UploadManifest mapUploadManifest(List<String> fileList, String senderReference) {
		MottakerInfo mottakerInfo = eformidlingMottakerInfoService.hentMottakerInfoTrygderetten();
		return UploadManifest.builder()
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
				.externalServiceCode(mottakerInfo.getServiceCode())
				.externalServiceEdictionCode(Integer.parseInt(mottakerInfo.getServiceEditionCode()))
				.build();
	}
}
