package no.nav.dokdisteformidling.consumer.eformidling.serviceregistry;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType
@XmlEnum
@RequiredArgsConstructor
@Getter
public enum ServiceIdentifier {

    @XmlEnumValue("DPO") DPO("DPO"),
    @XmlEnumValue("DPV") DPV("DPV"),
    @XmlEnumValue("DPI") DPI("DPI"),
    @XmlEnumValue("DPF") DPF("DPF"),
    @XmlEnumValue("DPE") DPE("DPE"),
    UNKNOWN("UNKNOWN");

    private final String fullname;
}
