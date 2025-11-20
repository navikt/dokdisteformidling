package no.nav.dokdisteformidling.consumer.eformidling.serviceregistry;

import no.nav.dokdisteformidling.certificate.AppCertificate;
import no.nav.dokdisteformidling.certificate.KeyStoreCredentials;
import no.nav.dokdisteformidling.certificate.KeyStoreProperties;
import no.nav.dokdisteformidling.config.props.MaskinportenProperties;
import no.nav.dokdisteformidling.config.props.ServiceRegistryProperties;
import no.nav.dokdisteformidling.consumer.eformidling.maskinporten.MaskinportenTokenConsumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.FileSystemResource;

import java.net.MalformedURLException;
import java.net.URI;

import static no.nav.dokdisteformidling.consumer.eformidling.EformidlingConstants.AVTALTMELDING_PROCESS;
import static no.nav.dokdisteformidling.consumer.eformidling.EformidlingConstants.TRYGDERETTEN_ORGNUMMER;

@Disabled("Manuell test")
class ServiceRegistryConsumerManualTest {

	private KeyStoreProperties keyStoreProperties;
	private KeyStoreCredentials keyStoreCredentials;
	private final MaskinportenProperties maskinportenProperties = new MaskinportenProperties();
	private final ServiceRegistryProperties serviceRegistryProperties = new ServiceRegistryProperties();

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
		maskinportenProperties.setClientid("MOVE_IP_991078045");
		//test
//		maskinportenProperties.setAudience("https://oidc-ver1.difi.no/idporten-oidc-provider/");
//		maskinportenProperties.setUrl(new URL("https://oidc-ver1.difi.no/idporten-oidc-provider/token"));
		//prod
		maskinportenProperties.setAudience("https://oidc.difi.no/idporten-oidc-provider/");
		maskinportenProperties.setUrl(URI.create("https://oidc.difi.no/idporten-oidc-provider/token").toURL());
		keyStoreCredentials = new KeyStoreCredentials(System.getProperty("virksomhetssertifikat.type"), System.getProperty("virksomhetssertifikat.alias"), System.getProperty("virksomhetssertifikat.password"));
		keyStoreProperties = new KeyStoreProperties(System.getProperty("virksomhetssertifikat.path"), "");
		//test
//		serviceRegistryProperties.setUrl(new URL("https://qa-meldingsutveksling.difi.no/serviceregistry/"));
		//prod
		serviceRegistryProperties.setUrl(URI.create("https://meldingsutveksling.difi.no/serviceregistry/").toURL());
	}

	@Test
	void shouldFetchMottakerInfoWhenSystemPropertiesSet() {
		MaskinportenTokenConsumer maskinportenTokenConsumer = new MaskinportenTokenConsumer(new AppCertificate(keyStoreProperties, keyStoreCredentials), maskinportenProperties, new RestTemplateBuilder());
		ServiceRegistryConsumer serviceRegistryConsumer = new ServiceRegistryConsumer(serviceRegistryProperties, maskinportenTokenConsumer, new RestTemplateBuilder());

		final IdentifierResource identifierResource = serviceRegistryConsumer.getIdentifierResource(TRYGDERETTEN_ORGNUMMER, AVTALTMELDING_PROCESS);
		System.out.println(identifierResource);
	}
}