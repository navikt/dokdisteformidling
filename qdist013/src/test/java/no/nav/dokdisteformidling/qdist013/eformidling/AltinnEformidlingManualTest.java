package no.nav.dokdisteformidling.qdist013.eformidling;

import no.nav.dokdisteformidling.certificate.KeyStoreProperties;
import no.nav.dokdisteformidling.config.props.MaskinportenProperties;
import no.nav.dokdisteformidling.config.props.ServiceRegistryProperties;
import no.nav.dokdisteformidling.qdist013.eformidling.dokumentpakker.EformidlingMessagePackager;
import no.nav.dokdisteformidling.qdist013.maskinporten.MaskinportenTokenConsumer;
import no.nav.dokdisteformidling.qdist013.serviceregistry.EformidlingMottakerInfoService;
import no.nav.dokdisteformidling.qdist013.serviceregistry.ServiceRegistryConsumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.FileSystemResource;

import java.io.ByteArrayInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Disabled("Manuell test")
class AltinnEformidlingManualTest {
	private KeyStoreProperties keyStoreProperties = new KeyStoreProperties();
	private MaskinportenProperties maskinportenProperties = new MaskinportenProperties();
	private ServiceRegistryProperties serviceRegistryProperties = new ServiceRegistryProperties();

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
	}

	@Test
	void shouldSendManualTest() {
		MaskinportenTokenConsumer maskinportenTokenConsumer = new MaskinportenTokenConsumer(keyStoreProperties, maskinportenProperties, new RestTemplateBuilder());
		ServiceRegistryConsumer serviceRegistryConsumer = new ServiceRegistryConsumer(serviceRegistryProperties, maskinportenTokenConsumer, new RestTemplateBuilder());
		EformidlingMottakerInfoService eformidlingMottakerInfoService = new EformidlingMottakerInfoService(serviceRegistryConsumer);
		EformidlingMessagePackager eformidlingMessagePackager = new EformidlingMessagePackager();
		final Eformidling eformidling = new AltinnEformidling(keyStoreProperties, eformidlingMottakerInfoService, eformidlingMessagePackager);

		final NavDokumentpakke navDokumentpakke = new NavDokumentpakke("arkivmelding", Collections.singletonList(NavDokument.builder()
				.filnavn("test1.pdf")
				.mimeType("application/pdf")
				.contents(new ByteArrayInputStream("dokument".getBytes()))
				.build()));

		eformidling.send(navDokumentpakke);
	}
}