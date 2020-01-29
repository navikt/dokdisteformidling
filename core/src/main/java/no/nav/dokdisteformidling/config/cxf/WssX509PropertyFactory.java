package no.nav.dokdisteformidling.config.cxf;

import lombok.Getter;
import lombok.Setter;
import no.nav.dokdisteformidling.certificate.KeyStoreProperties;
import no.nav.dokdisteformidling.config.props.DpoUserProperties;

import java.io.IOException;
import java.util.Properties;

public final class WssX509PropertyFactory {
    private WssX509PropertyFactory(){
        // noop
    }

    public static Properties createWssX509TokenProperties(final KeyStoreProperties keyStoreProperties) throws IOException {
        Properties properties = new Properties();
        properties.setProperty("org.apache.ws.security.crypto.provider", "org.apache.ws.security.components.crypto.Merlin");
        properties.setProperty("org.apache.ws.security.crypto.merlin.keystore.file", keyStoreProperties.getPath().getURI().toString());
        properties.setProperty("org.apache.ws.security.crypto.merlin.keystore.password", keyStoreProperties.getPassword());
        properties.setProperty("org.apache.ws.security.crypto.merlin.keystore.type", keyStoreProperties.getType());
        properties.setProperty("org.apache.ws.security.crypto.merlin.keystore.private.password", keyStoreProperties.getPassword());
        properties.setProperty("org.apache.ws.security.crypto.merlin.keystore.alias", keyStoreProperties.getAlias());
        return properties;
    }
}
