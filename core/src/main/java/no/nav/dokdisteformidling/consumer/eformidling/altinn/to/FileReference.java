package no.nav.dokdisteformidling.consumer.eformidling.altinn.to;

import lombok.Value;

@Value
public class FileReference {

    private String fileReference;
    private int receiptsId;
}
