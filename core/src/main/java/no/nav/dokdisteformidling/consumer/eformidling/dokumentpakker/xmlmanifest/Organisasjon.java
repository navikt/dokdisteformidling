package no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.xmlmanifest;

import static no.nav.dokdisteformidling.consumer.eformidling.Organisasjonsnummer.ISO6523_AUTHORITY;
import static no.nav.dokdisteformidling.consumer.eformidling.Organisasjonsnummer.ISO6523_PREFIX;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.XmlValue;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "organisasjon")
@XmlRootElement(name = "organiasjon")
public class Organisasjon {
    @XmlAttribute
    private String authority;
    @XmlValue
    private String orgNummer;

    public Organisasjon(final String orgNummer) {
        super();
        this.authority = ISO6523_AUTHORITY;
        this.orgNummer = ISO6523_PREFIX + orgNummer;
    }

    public Organisasjon() {
        super();
    }

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }

    public String getOrgNummer() {
        return orgNummer;
    }

    public void setOrgNummer(String orgNummer) {
        this.orgNummer = orgNummer;
    }

}
