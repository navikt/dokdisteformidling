package no.nav.dokdisteformidling.consumer.eformidling;

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
import java.net.URI;
import java.time.Clock;
import java.util.UUID;

import static java.util.Collections.singletonList;
import static no.nav.dokdisteformidling.constants.DomainConstants.DEFAULT_ZONE_ID;

@Disabled("Manuell test")
class AltinnEformidlingManualTest {

	private static final Clock SYSTEM_CLOCK = Clock.system(DEFAULT_ZONE_ID);

	private final KeyStoreProperties keyStoreProperties = new KeyStoreProperties();
	private final MaskinportenProperties maskinportenProperties = new MaskinportenProperties();
	private final ServiceRegistryProperties serviceRegistryProperties = new ServiceRegistryProperties();
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
		maskinportenProperties.setClientid("MOVE_IP_991078045");
		maskinportenProperties.setUrl(URI.create("https://oidc-ver1.difi.no/idporten-oidc-provider/token").toURL());
		keyStoreProperties.setType(System.getProperty("virksomhetssertifikat.type"));
		keyStoreProperties.setAlias(System.getProperty("virksomhetssertifikat.alias"));
		keyStoreProperties.setPassword(System.getProperty("virksomhetssertifikat.password"));
		keyStoreProperties.setPath(new FileSystemResource(System.getProperty("virksomhetssertifikat.path")));
		serviceRegistryProperties.setUrl(URI.create("https://qa-meldingsutveksling.difi.no/serviceregistry/").toURL());
		appCertificate = new AppCertificate(keyStoreProperties);
	}

	@Test
	void shouldSendManualTest() {
		MaskinportenTokenConsumer maskinportenTokenConsumer = new MaskinportenTokenConsumer(appCertificate, maskinportenProperties, new RestTemplateBuilder());
		ServiceRegistryConsumer serviceRegistryConsumer = new ServiceRegistryConsumer(serviceRegistryProperties, maskinportenTokenConsumer, new RestTemplateBuilder());
		EformidlingMottakerInfoService eformidlingMottakerInfoService = new EformidlingMottakerInfoService(serviceRegistryConsumer);
		EformidlingMessagePackager eformidlingMessagePackager = new EformidlingMessagePackager(
				new JacksonConfig().eformidlingObjectMapper(SYSTEM_CLOCK),
				new StandardBusinessDocumentMapper(SYSTEM_CLOCK),
				new EformidlingContentPackager());

		final NavDokumentpakke navDokumentpakke = NavDokumentpakke.builder()
				.conversationId("1")
				.bestillingsId("2")
				.messageChannelInstanceIdentifier(UUID.randomUUID())
				.arkivmelding(NavDokument.fromAvtaltmelding(new ByteArrayInputStream("arkivmelding".getBytes())))
				.navDokumenter(singletonList(NavDokument.fromVedlegg("test1.pdf", new ByteArrayInputStream("test1pdf".getBytes()))))
				.build();

	}
}