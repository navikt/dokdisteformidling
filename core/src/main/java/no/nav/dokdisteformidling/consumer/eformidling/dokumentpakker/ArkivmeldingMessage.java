package no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Endret og tilpasset for NAV sin bruk fra https://github.com/difi/move-integrasjonspunkt
 *
 * Representerer en eformidling arkivmelding.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@XmlRootElement(name = "arkivmelding", namespace = "urn:no:difi:meldingsutveksling:2.0")
public class ArkivmeldingMessage extends BusinessMessage<ArkivmeldingMessage> {

}
