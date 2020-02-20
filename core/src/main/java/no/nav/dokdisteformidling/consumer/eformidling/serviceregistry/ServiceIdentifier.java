package no.nav.dokdisteformidling.consumer.eformidling.serviceregistry;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ServiceIdentifier {
    DPO("DPO"),
    DPV("DPV"),
    DPI("DPI"),
    DPF("DPF"),
    DPE("DPE"),
    UNKNOWN("UNKNOWN");

    private final String fullname;
}
