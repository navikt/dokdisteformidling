package no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.xmlmanifest;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "organisasjon")
@XmlRootElement(name = "organiasjon")
public class Organisasjon {
    private static final String ISO6523_AUTHORITY = "iso6523-actorid-upis";
    private static final String ISO6523_PREFIX = "0192:";

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
