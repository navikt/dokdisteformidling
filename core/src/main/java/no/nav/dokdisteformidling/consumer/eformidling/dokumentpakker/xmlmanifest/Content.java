package no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.xmlmanifest;

import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "content", namespace = "urn:no:difi:meldingsutveksling:1.0")
@NoArgsConstructor
public class Content {

    @XmlValue
    @SuppressWarnings("squid:S1700")
    String content;

    public Content(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }
}
