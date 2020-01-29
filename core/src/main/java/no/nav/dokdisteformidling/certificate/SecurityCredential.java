package no.nav.dokdisteformidling.certificate;

import lombok.Getter;
import lombok.Setter;
import no.nav.dokdisteformidling.config.props.DpoUserProperties;

import java.util.Properties;

@Getter
@Setter
public class SecurityCredential {

    private final Properties properties;
    private DpoUserProperties dpoUserProperties;
    private String enhet;

    public SecurityCredential(final KeyStoreProperties keyStoreProperties, String enhet, final DpoUserProperties dpoUserProperties) {
        this.dpoUserProperties = dpoUserProperties;
        this.enhet = enhet;
        properties = new Properties();
        properties.setProperty("org.apache.ws.security.crypto.provider", "org.apache.ws.security.components.crypto.Merlin");
        properties.setProperty("org.apache.ws.security.crypto.merlin.keystore.file", keyStoreProperties.getPath().getFilename());
        properties.setProperty("org.apache.ws.security.crypto.merlin.keystore.password", keyStoreProperties.getPassword());
        properties.setProperty("org.apache.ws.security.crypto.merlin.keystore.type", keyStoreProperties.getType());
        properties.setProperty("org.apache.ws.security.crypto.merlin.keystore.private.password", keyStoreProperties.getPassword());
        properties.setProperty("org.apache.ws.security.crypto.merlin.keystore.alias", keyStoreProperties.getAlias());
    }
}
