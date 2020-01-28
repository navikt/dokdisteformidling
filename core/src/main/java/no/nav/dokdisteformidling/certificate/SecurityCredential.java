package no.nav.dokdisteformidling.certificate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import no.nav.dokdisteformidling.config.props.DpoUserProperties;

import java.util.Properties;

@Getter
@NoArgsConstructor
public class SecurityCredential {
    private  KeyStoreProperties keyStoreProperties;
    private  DpoUserProperties dpoUserProperties;
    private Properties properties;
    private String orgNummer;

    public SecurityCredential(DpoUserProperties dpoUserProperties, KeyStoreProperties keyStoreProperties) throws KeystoreProviderException {
        this.dpoUserProperties = dpoUserProperties;
        this.keyStoreProperties = keyStoreProperties;
        properties = new Properties();
        properties.setProperty("org.apache.ws.security.crypto.provider", "org.apache.ws.security.components.crypto.Merlin");
        properties.setProperty("org.apache.ws.security.crypto.merlin.keystore.file", keyStoreProperties.getPath().getFilename());
        properties.setProperty("org.apache.ws.security.crypto.merlin.keystore.password", keyStoreProperties.getPassword());
        properties.setProperty("org.apache.ws.security.crypto.merlin.keystore.type", keyStoreProperties.getType());
        properties.setProperty("org.apache.ws.security.crypto.merlin.keystore.private.password", keyStoreProperties.getPassword());
        properties.setProperty("org.apache.ws.security.crypto.merlin.keystore.alias", keyStoreProperties.getAlias());
    }
}
