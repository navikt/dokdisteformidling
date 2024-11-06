package no.nav.dokdisteformidling.consumer.eformidling.altinn.services;

import no.altinn.brokerserviceexternal.BrokerServiceInitiation;
import no.altinn.brokerserviceexternal.File;
import no.altinn.brokerserviceexternal.IBrokerServiceExternal;
import no.nav.dokdisteformidling.consumer.eformidling.altinn.to.UploadManifest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static no.nav.dokdisteformidling.consumer.eformidling.EformidlingConstants.NAV_ORGNUMMER;
import static no.nav.dokdisteformidling.consumer.eformidling.EformidlingConstants.TRYGDERETTEN_ORGNUMMER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BrokerServiceExternalServiceTest {

	private static final String FILE_ZIP_NAME = "sbd.zip";
	private static final String SENDER_REFERENCE = "1";
	private static final String SERVICE_CODE = "4192";
	private static final String SERVICE_EDITION_CODE = "270815";
	private static final String FILE_REFERENCE = "1234";

	@Mock
	IBrokerServiceExternal iBrokerServiceExternalMock;
	@InjectMocks
	BrokerServiceExternalService brokerServiceExternalService;
	final ArgumentCaptor<BrokerServiceInitiation> brokerServiceInitiationArgumentCaptor = ArgumentCaptor.forClass(BrokerServiceInitiation.class);

	@Test
	void shouldInitiateBrokerService() throws Exception {
		when(iBrokerServiceExternalMock.initiateBrokerService(brokerServiceInitiationArgumentCaptor.capture())).thenReturn(FILE_REFERENCE);

		final String fileReference = brokerServiceExternalService.intiateBrokerService(createUploadManifest());
		assertThat(fileReference).isEqualTo(FILE_REFERENCE);
		final BrokerServiceInitiation brokerServiceInitiation = brokerServiceInitiationArgumentCaptor.getValue();
		assertThat(brokerServiceInitiation.getManifest())
				.extracting("externalServiceCode", "externalServiceEditionCode", "reportee", "sendersReference")
				.contains(SERVICE_CODE, Integer.parseInt(SERVICE_EDITION_CODE), NAV_ORGNUMMER, SENDER_REFERENCE);
		assertThat(brokerServiceInitiation.getManifest().getPropertyList())
				.isNull();
		assertThat(brokerServiceInitiation.getManifest().getFileList().getValue().getFile())
				.extracting(File::getFileName)
				.contains(FILE_ZIP_NAME);

		assertThat(brokerServiceInitiation.getRecipientList().getRecipient())
				.extracting("partyNumber")
				.contains(TRYGDERETTEN_ORGNUMMER);
	}

	private UploadManifest createUploadManifest() {
		return UploadManifest.builder()
				.avsender(NAV_ORGNUMMER)
				.fileZipName(FILE_ZIP_NAME)
				.senderReference(SENDER_REFERENCE)
				.serviceCode(SERVICE_CODE)
				.serviceEditionCode(SERVICE_EDITION_CODE)
				.build();
	}

}