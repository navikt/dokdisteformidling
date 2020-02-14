package no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import net.minidev.json.annotate.JsonIgnore;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@XmlRootElement(name = "arkivmelding_kvittering", namespace = "urn:no:difi:meldingsutveksling:2.0")
public class ArkivmeldingKvitteringMessage extends BusinessMessage<ArkivmeldingKvitteringMessage> {

    private String receiptType;
    private String relatedToMessageId;
    private Set<KvitteringStatusMessage> message;

    @JsonIgnore
    public ArkivmeldingKvitteringMessage addMessage(KvitteringStatusMessage message) {
        if (this.message == null) {
            this.message = new HashSet<>();
        }
        this.message.add(message);
        return this;
    }
}
