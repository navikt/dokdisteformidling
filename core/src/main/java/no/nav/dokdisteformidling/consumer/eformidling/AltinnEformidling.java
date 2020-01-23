package no.nav.dokdisteformidling.consumer.eformidling;


import lombok.extern.slf4j.Slf4j;
import no.nav.dokdisteformidling.certificate.AppCertificate;
import no.nav.dokdisteformidling.certificate.KeyStoreProperties;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.EformidlingMessagePackager;
import no.nav.dokdisteformidling.consumer.eformidling.serviceregistry.EformidlingMottakerInfoService;
import no.nav.dokdisteformidling.consumer.eformidling.serviceregistry.MottakerInfo;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
@Slf4j
class AltinnEformidling implements Eformidling {
	private final KeyStoreProperties keyStoreProperties;
	private final EformidlingMottakerInfoService eformidlingMottakerInfoService;
	private final EformidlingMessagePackager eformidlingMessagePackager;

	@Inject
	AltinnEformidling(KeyStoreProperties keyStoreProperties,
					  EformidlingMottakerInfoService eformidlingMottakerInfoService,
					  EformidlingMessagePackager eformidlingMessagePackager) {
		this.keyStoreProperties = keyStoreProperties;
		this.eformidlingMottakerInfoService = eformidlingMottakerInfoService;
		this.eformidlingMessagePackager = eformidlingMessagePackager;
	}

	@Override
	public void send(NavDokumentpakke navDokumentpakke) {
		final MottakerInfo mottakerInfo = eformidlingMottakerInfoService.hentMottakerInfoTrygderetten();
		final InputStream kryptertEformidlingDokumentpakke = eformidlingMessagePackager.createEformidlingMessage(navDokumentpakke,
				new AppCertificate(keyStoreProperties), mottakerInfo.getX509Certificate());

		// TODO Altinn BrokerServiceExternalEC2.InitiateBrokerServiceEC
		// TODO Altinn BrokerServiceExternalEC2Streamed.UploadFileStreamedEC
	}
}
