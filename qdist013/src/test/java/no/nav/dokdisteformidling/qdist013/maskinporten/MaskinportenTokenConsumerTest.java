package no.nav.dokdisteformidling.qdist013.maskinporten;

import no.nav.dokdisteformidling.certificate.KeyStoreProperties;
import no.nav.dokdisteformidling.config.props.MaskinportenProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.FileSystemResource;

import java.net.MalformedURLException;
import java.net.URL;

@Disabled("Manuell test")
public class MaskinportenTokenConsumerTest {
	private MaskinportenProperties props = new MaskinportenProperties();

	@BeforeEach
	public void setup() throws MalformedURLException {
		// Sett system properties VM options for testen. Ikke putt det i koden.
		//
		// javax.net.ssl.trustStore
		// javax.net.ssl.trustStorePassword
		// maskinporten.keystore.type
		// maskinporten.keystore.alias
		// maskinporten.keystore.password
		// maskinporten.keystore.path
		System.setProperty("https.proxyHost", "webproxy-utvikler.nav.no");
		System.setProperty("https.proxyPort", "8088");
		System.setProperty("https.nonProxyHosts", "*.155.55.|*.192.168.|*.10.|*.local|*.rtv.gov|*.adeo.no|*.nav.no|*.aetat.no|*.devillo.no|*.oera.no");
		props.setAudience("https://oidc-ver1.difi.no/idporten-oidc-provider/");
		props.setClientid("MOVE_IP_889640782");
		props.setUrl(new URL("https://oidc-ver1.difi.no/idporten-oidc-provider/token"));
		KeyStoreProperties keyStoreProperties = new KeyStoreProperties();
		keyStoreProperties.setType(System.getProperty("maskinporten.keystore.type"));
		keyStoreProperties.setAlias(System.getProperty("maskinporten.keystore.alias"));
		keyStoreProperties.setPassword(System.getProperty("maskinporten.keystore.password"));
		keyStoreProperties.setPath(new FileSystemResource(System.getProperty("maskinporten.keystore.path")));
		props.setKeystore(keyStoreProperties);
	}

	@Test
	public void shouldFetchTokenWhenSystemPropertiesSet() {
		MaskinportenTokenConsumer oidcTokenClient = new MaskinportenTokenConsumer(props, new RestTemplateBuilder());

		final OidcTokenResponse oidcTokenResponse = oidcTokenClient.fetchToken();
		System.out.println(oidcTokenResponse.getAccessToken());
	}
}
