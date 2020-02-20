package no.nav.dokdisteformidling.consumer.eformidling.serviceregistry;

import no.nav.dokdisteformidling.certificate.AppCertificate;
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
import java.net.URL;

import static no.nav.dokdisteformidling.consumer.eformidling.EformidlingConstants.ARKIVMELDING_PROCESS;
import static no.nav.dokdisteformidling.consumer.eformidling.EformidlingConstants.TRYGDERETTEN_ORGNUMMER;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Disabled("Manuell test")
class ServiceRegistryConsumerManualTest {
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
		maskinportenProperties.setClientid("MOVE_IP_889640782");
		//test
//		maskinportenProperties.setAudience("https://oidc-ver1.difi.no/idporten-oidc-provider/");
//		maskinportenProperties.setUrl(new URL("https://oidc-ver1.difi.no/idporten-oidc-provider/token"));
		//prod
		maskinportenProperties.setAudience("https://oidc.difi.no/idporten-oidc-provider/");
		maskinportenProperties.setUrl(new URL("https://oidc.difi.no/idporten-oidc-provider/token"));
		keyStoreProperties.setType(System.getProperty("virksomhetssertifikat.type"));
		keyStoreProperties.setAlias(System.getProperty("virksomhetssertifikat.alias"));
		keyStoreProperties.setPassword(System.getProperty("virksomhetssertifikat.password"));
		keyStoreProperties.setPath(new FileSystemResource(System.getProperty("virksomhetssertifikat.path")));
		//test
//		serviceRegistryProperties.setUrl(new URL("https://qa-meldingsutveksling.difi.no/serviceregistry/"));
		//prod
		serviceRegistryProperties.setUrl(new URL("https://meldingsutveksling.difi.no/serviceregistry/"));
	}

	@Test
	public void shouldFetchMottakerInfoWhenSystemPropertiesSet() {
		MaskinportenTokenConsumer maskinportenTokenConsumer = new MaskinportenTokenConsumer(new AppCertificate(keyStoreProperties), maskinportenProperties, new RestTemplateBuilder());
		ServiceRegistryConsumer serviceRegistryConsumer = new ServiceRegistryConsumer(serviceRegistryProperties, maskinportenTokenConsumer, new RestTemplateBuilder());

		final IdentifierResource identifierResource = serviceRegistryConsumer.getIdentifierResource(TRYGDERETTEN_ORGNUMMER, ARKIVMELDING_PROCESS);
		System.out.println(identifierResource);
	}
}