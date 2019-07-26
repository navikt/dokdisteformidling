package no.nav.dokdisteformidling.consumer.integrasjonspunkt;

import lombok.Builder;
import lombok.Value;
import org.unece.cefact.namespaces.standardbusinessdocumentheader.StandardBusinessDocumentHeader;

/**
 * @author Heidi Elisabeth Sando, Visma Consulting AS
 */

@Value
@Builder
public class IntegrasjonspunktRequestTo {

	public final Any any; //Todo:  Heter ikke any, men en av følgende: arkivmelding, digital, digital_dpv, print, innsynskrav eller publisering. Hvordan få dette inn?
	public final StandardBusinessDocumentHeader standardBusinessDocumentHeader;

	@Value
	@Builder
	public final static class Any {        //TODO Begge feltene må avklares
		private final String hoveddokument;
		private final int sikkerhetsnivaa;
	}
}
