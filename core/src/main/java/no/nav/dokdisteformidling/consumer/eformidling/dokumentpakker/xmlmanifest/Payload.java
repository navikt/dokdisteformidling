package no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.xmlmanifest;

import lombok.NoArgsConstructor;
import org.apache.commons.codec.binary.Base64;

import javax.xml.bind.annotation.*;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "payload")
@XmlRootElement(name = "payload", namespace = "urn:no:difi:meldingsutveksling:1.0")
@NoArgsConstructor
public class Payload {

    @XmlElement(namespace = "urn:no:difi:meldingsutveksling:1.0")
    private Content content;

    @XmlTransient
    private InputStream inputStream;

    public Payload(byte[] payload) {
        this.content = new Content(new String(Base64.encodeBase64(payload), StandardCharsets.UTF_8));
    }

    public String getContent() {
        return content.getContent();
    }

    public InputStream getInputStream() {
        return this.inputStream;
    }
}
