package no.nav.dokdisteformidling.consumer.eformidling.maskinporten;

import no.nav.dokdisteformidling.certificate.AppCertificate;
import no.nav.dokdisteformidling.certificate.KeyStoreProperties;
import no.nav.dokdisteformidling.config.props.MaskinportenProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.FileSystemResource;

import java.net.MalformedURLException;
import java.net.URI;

@Disabled("Manuell test")
class MaskinportenTokenConsumerManualTest {

    private final KeyStoreProperties keyStoreProperties = new KeyStoreProperties();
    private final MaskinportenProperties maskinportenProperties = new MaskinportenProperties();

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
        // test
//        maskinportenProperties.setAudience("https://oidc-ver1.difi.no/idporten-oidc-provider/");
//        maskinportenProperties.setUrl(new URL("https://oidc-ver1.difi.no/idporten-oidc-provider/token"));
        // prod
        maskinportenProperties.setAudience("https://oidc.difi.no/idporten-oidc-provider/");
        maskinportenProperties.setUrl(URI.create("https://oidc.difi.no/idporten-oidc-provider/token").toURL());
        keyStoreProperties.setType(System.getProperty("virksomhetssertifikat.type"));
        keyStoreProperties.setAlias(System.getProperty("virksomhetssertifikat.alias"));
        keyStoreProperties.setPassword(System.getProperty("virksomhetssertifikat.password"));
        keyStoreProperties.setPath(new FileSystemResource(System.getProperty("virksomhetssertifikat.path")));
    }

    @Test
    void shouldFetchTokenWhenSystemPropertiesSet() {
        MaskinportenTokenConsumer oidcTokenClient = new MaskinportenTokenConsumer(new AppCertificate(keyStoreProperties), maskinportenProperties, new RestTemplateBuilder());

        final OidcTokenResponse oidcTokenResponse = oidcTokenClient.fetchToken();
        System.out.println(oidcTokenResponse.getAccessToken());
    }
}
