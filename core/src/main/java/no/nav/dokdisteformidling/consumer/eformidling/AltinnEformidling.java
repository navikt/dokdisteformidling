package no.nav.dokdisteformidling.consumer.eformidling;


import lombok.extern.slf4j.Slf4j;
import no.nav.dokdisteformidling.certificate.AppCertificate;
import no.nav.dokdisteformidling.consumer.eformidling.brokerserviceexternalec2.services.BrokerServiceExternalService;
import no.nav.dokdisteformidling.consumer.eformidling.brokerserviceexternalec2.to.ServiceCode;
import no.nav.dokdisteformidling.consumer.eformidling.brokerserviceexternalec2.to.UploadManifest;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.EformidlingMessagePackager;
import no.nav.dokdisteformidling.consumer.eformidling.serviceregistry.EformidlingMottakerInfoService;
import no.nav.dokdisteformidling.consumer.eformidling.serviceregistry.MottakerInfo;
import org.springframework.stereotype.Component;

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
	private static final String FILE_NAME = "sbd.zip";

	@Inject
	AltinnEformidling(AppCertificate appCertificate,
					  EformidlingMottakerInfoService eformidlingMottakerInfoService,
					  EformidlingMessagePackager eformidlingMessagePackager, BrokerServiceExternalService brokerServiceExternalService) {
		this.appCertificate = appCertificate;
		this.eformidlingMottakerInfoService = eformidlingMottakerInfoService;
		this.eformidlingMessagePackager = eformidlingMessagePackager;
		this.brokerServiceExternalService = brokerServiceExternalService;
	}

	@Override
	public void send(NavDokumentpakke navDokumentpakke) {
		final MottakerInfo mottakerInfo = eformidlingMottakerInfoService.hentMottakerInfoTrygderetten();
		final InputStream sbdZip = eformidlingMessagePackager.packageMessage(navDokumentpakke,
				appCertificate, mottakerInfo.getX509Certificate());

		// TODO Altinn BrokerServiceExternalEC2.InitiateBrokerServiceEC(navDokumentpakke, mottakerInfo)
		// TODO Altinn BrokerServiceExternalEC2Streamed.UploadFileStreamedEC(sbdZip)
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
				.externalServiceEdictionCode(Integer.valueOf(mottakerInfo.getServiceEditionCode()))
				.build();
	}
}
