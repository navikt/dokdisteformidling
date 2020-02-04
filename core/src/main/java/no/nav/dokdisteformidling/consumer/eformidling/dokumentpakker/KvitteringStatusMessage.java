package no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker;

import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class KvitteringStatusMessage extends AbstractEntity<Long> {
    private String code;
    private String text;
}
