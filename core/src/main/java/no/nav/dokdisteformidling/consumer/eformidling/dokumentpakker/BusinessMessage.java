package no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Endret og tilpasset for NAV sin bruk fra https://github.com/difi/move-integrasjonspunkt
 *
 * Representerer en eformidling forretningsmelding.
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public abstract class BusinessMessage<T extends BusinessMessage<T>> {
	private Integer sikkerhetsnivaa;
	private String hoveddokument;
}
