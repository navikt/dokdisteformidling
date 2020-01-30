package no.nav.dokdisteformidling.consumer.eformidling;

import static no.nav.dokdisteformidling.constants.DomainConstants.DEFAULT_ZONE_ID;

import no.nav.dokdisteformidling.certificate.AppCertificate;
import no.nav.dokdisteformidling.certificate.KeyStoreProperties;
import no.nav.dokdisteformidling.config.props.MaskinportenProperties;
import no.nav.dokdisteformidling.config.props.ServiceRegistryProperties;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.EformidlingContentPackager;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.EformidlingMessagePackager;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.JacksonConfig;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.StandardBusinessDocumentMapper;
import no.nav.dokdisteformidling.consumer.eformidling.maskinporten.MaskinportenTokenConsumer;
import no.nav.dokdisteformidling.consumer.eformidling.serviceregistry.EformidlingMottakerInfoService;
import no.nav.dokdisteformidling.consumer.eformidling.serviceregistry.ServiceRegistryConsumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.FileSystemResource;

import java.io.ByteArrayInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Clock;
import java.util.Collections;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Disabled("Manuell test")
class AltinnEformidlingManualTest {
	private static final Clock SYSTEM_CLOCK = Clock.system(DEFAULT_ZONE_ID);

	private KeyStoreProperties keyStoreProperties = new KeyStoreProperties();
	private MaskinportenProperties maskinportenProperties = new MaskinportenProperties();
	private ServiceRegistryProperties serviceRegistryProperties = new ServiceRegistryProperties();
	private AppCertificate appCertificate;

	@BeforeEach
	public void setup() throws MalformedURLException {
		// Sett system properties VM options for testen. Ikke putt det i koden.
		//
		// javax.net.ssl.trustStore
		// javax.net.ssl.trustStorePassword
		// virksomhetssertifikat.type
		// virksomhetssertifikat.alias
		// virksomhetssertifikat.password
		// virksomhetssertifikat.path
		System.setProperty("https.proxyHost", "webproxy-utvikler.nav.no");
		System.setProperty("https.proxyPort", "8088");
		System.setProperty("https.nonProxyHosts", "*.155.55.|*.192.168.|*.10.|*.local|*.rtv.gov|*.adeo.no|*.nav.no|*.aetat.no|*.devillo.no|*.oera.no");
		maskinportenProperties.setAudience("https://oidc-ver1.difi.no/idporten-oidc-provider/");
		maskinportenProperties.setClientid("MOVE_IP_889640782");
		maskinportenProperties.setUrl(new URL("https://oidc-ver1.difi.no/idporten-oidc-provider/token"));
		keyStoreProperties.setType(System.getProperty("virksomhetssertifikat.type"));
		keyStoreProperties.setAlias(System.getProperty("virksomhetssertifikat.alias"));
		keyStoreProperties.setPassword(System.getProperty("virksomhetssertifikat.password"));
		keyStoreProperties.setPath(new FileSystemResource(System.getProperty("virksomhetssertifikat.path")));
		serviceRegistryProperties.setUrl(new URL("https://qa-meldingsutveksling.difi.no/serviceregistry/"));
		appCertificate = new AppCertificate(keyStoreProperties);
	}

	@Test
	void shouldSendManualTest() {
		MaskinportenTokenConsumer maskinportenTokenConsumer = new MaskinportenTokenConsumer(appCertificate, maskinportenProperties, new RestTemplateBuilder());
		ServiceRegistryConsumer serviceRegistryConsumer = new ServiceRegistryConsumer(serviceRegistryProperties, maskinportenTokenConsumer, new RestTemplateBuilder());
		EformidlingMottakerInfoService eformidlingMottakerInfoService = new EformidlingMottakerInfoService(serviceRegistryConsumer);
		EformidlingMessagePackager eformidlingMessagePackager = new EformidlingMessagePackager(
				new JacksonConfig().eformidlingObjectMapper(),
				new StandardBusinessDocumentMapper(SYSTEM_CLOCK),
				new EformidlingContentPackager());
		final Eformidling eformidling = new AltinnEformidling(appCertificate, eformidlingMottakerInfoService, eformidlingMessagePackager, null);

		final NavDokumentpakke navDokumentpakke = NavDokumentpakke.builder()
				.conversationId("1")
				.bestillingsId("2")
				.arkivmelding(NavDokument.fromArkivmelding(new ByteArrayInputStream("arkivmelding".getBytes())))
				.navDokumenter(Collections.singletonList(NavDokument.fromVedlegg("test1.pdf", new ByteArrayInputStream("test1pdf".getBytes()))))
				.build();

		eformidling.send(navDokumentpakke);
	}
}